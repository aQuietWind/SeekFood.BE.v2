# User 模块：用户注册登录与个人信息管理

## 1. 这个模块是干什么的

User 模块是整个 SeekFood 系统里负责"人"的部分。任何一个用户来到这个平台，第一步就是注册一个账号，然后登录、改头像、改密码、改资料，甚至最后注销账号——这些事情全部由 User 模块来管。

可以把它想象成一个商场的会员中心：你进门先办会员卡（注册），每次来逛的时候刷卡（登录），想换照片、改密码、退会都找它。这个模块端口是 **10001**，在 Nacos 注册中心里叫 **user**。

它不单打独斗，依赖了三个兄弟模块：
- **Config 模块**：提供所有可动态刷新的配置（Nacos 配置中心），比如 Redis Key 的名字、冷却时间、Caffeine 缓存参数等
- **DTO 模块**：定义了 UserDTO 这个数据传输对象，各个模块之间传用户信息都用它
- **Util 模块**：提供通用工具，比如 JWT 生成、验证码、文件保存、Redis 操作、MQ 消息发送等

---

## 2. 用户注册登录的完整流程

### 2.1 注册：从发验证码到创建账号

注册分两步走，先拿验证码，再提交注册。

**第一步：获取验证码**

用户输入手机号，点"获取验证码"。系统会先做两件事：
- 校验手机号格式对不对（`CommonParamRulesConfig.phoneNumberCheck`）
- 生成一个 6 位数字验证码，存到 Redis 里，设一个过期时间（比如 5 分钟）

验证码存 Redis 的 Key 格式是 `{registerOpt前缀}{手机号}`，这样每个手机号独立存储，互不影响。

**第二步：提交注册**

用户填好手机号、密码、验证码，点"注册"。系统按顺序做这些事情：

1. **格式校验**：手机号格式、密码格式都要过一遍
2. **验证码检查**：去 Redis 里根据 Key 取出验证码，和用户提交的比对，对不上或过期就直接拒绝
3. **冷却期检查**：所谓"冷却期"，就是同一个手机号在短时间内不能反复注册。系统会查 Redis 里有没有这个手机号的注册冷却记录，有的话就拒绝，防止别人用脚本恶意刷注册
4. **生成用户 ID**：通过 Redis 的自增计数器生成一个全局唯一的用户 ID。这个计数器在服务启动时初始化，如果 Redis 里还没有这个 Key，就设一个初始值（`idCapacity`），保证 ID 不会冲突
5. **写入数据库**：调用 `RegisterMapper.insertUser` 把用户数据插到 MySQL。注意这里**手机号字段有唯一索引**（`UNIQUE KEY uk_phone`），如果手机号重复，数据库会直接报唯一约束冲突，这是第一层保障。业务层在校验时虽然没有显式检查手机号是否存在，但这里有一个精妙的设计：**数据库唯一索引是最后一道防线**，如果手机号真的重复了，数据库会抛出异常，事务回滚，不会产生脏数据
6. **发送注册成功消息**：通过 RabbitMQ 发一条消息，告诉资金模块（Fund 模块）给新用户创建资金账户。这一步是异步的，不影响注册接口的响应速度

> 为什么手机号唯一性校验主要靠数据库？因为在高并发场景下，业务层哪怕先查一次再插入，也依然存在"查的时候没有、插的时候别人刚插完"的并发窗口。数据库唯一索引是原子操作，不存在这个窗口，是真正的最后一道防线。当然，更完善的方案是业务层也查一次，给用户友好的错误提示——但这里作者选择了"相信数据库唯一索引"，把错误信息统一处理。

### 2.2 登录：两种方式，殊途同归

登录支持两种方式：**验证码登录**和**密码登录**。

**验证码登录**：和注册获取验证码一样，先调接口拿到验证码，然后调登录接口，传手机号 + 验证码。系统验证通过后，根据手机号查出用户信息，生成 JWT Token 返回。

**密码登录**：用户传手机号 + 密码。系统在查数据库之前，先检查这个手机号有没有在密码登录冷却期内——这个冷却期专门用来防止暴力破解密码。如果连续输错密码被锁定，就要等冷却期过了才能再试。

无论是哪种登录方式，最后都会走到同一个方法 `loginAndGetToken`：
- 从数据库查出用户信息（`UserDTO`）
- 调用 `TokenUtil.getAndRecordToken` 生成 JWT Token
- 把 Token 存到 Redis（用于后续会话管理和多设备登录控制）
- 把 Token 设置到 HTTP 响应头里返回给前端

还有一个 **Token 刷新接口**，前端可以在 Token 快过期时调这个接口换一个新的，但同样有冷却期限制，防止频繁刷新。

### 2.3 整体流程图

```
注册：获取验证码 → 填信息提交 → 格式校验 → 验证码校验 → 冷却期检查 → 生成ID → 写入DB → 发MQ通知资金模块
登录：获取验证码 → 填信息提交 → 校验 → 查DB → 生成JWT → 存Redis → 返回Token
密码登录：填手机号+密码 → 冷却期检查 → 查DB比对密码 → 生成JWT → 存Redis → 返回Token
```

---

## 3. 缓存设计：为什么用 Caffeine + Redis

### 3.1 先搞清楚"缓存"到底解决什么问题

用户模块里有一个高频操作：**查看用户信息**。比如你看一个商家的评价，每个评价旁边都要显示用户头像和昵称——如果每次都要去数据库查，那数据库的压力就太大了。

所以需要缓存。但缓存放哪里？有两个选择：
- **放 Redis**：优点是多台服务器共享，缺点是每次都要走网络，有网络开销
- **放 JVM 内存**：优点是极快（纳秒级），缺点是每台服务器各自存一份，数据不一致

### 3.2 这个模块的做法：两级缓存，各取所长

User 模块用了一个**两级缓存**的设计：

```
请求 → Caffeine（JVM本地缓存） → 没有 → Redis → 没有 → MySQL
         ↑ 命中直接返回          ↑ 命中则回填Caffeine   ↑ 查出来回填Redis和Caffeine
```

**第一级：Caffeine（JVM 本地缓存）**

Caffeine 是当前 Java 生态里性能最好的本地缓存库。它直接存在 JVM 堆内存里，读取速度极快。在 User 模块里，有两个 Caffeine 实例：
- `UserCaffeine`：缓存用户信息（`UserDTO`），Key 是 `userId`
- `PhoneCaffeine`：缓存手机号，Key 是 `userId`，Value 是手机号字符串

Caffeine 的容量和过期时间都通过 Nacos 配置中心动态控制（`UserCaffeineConfig`），不需要重启服务就能调整。

**第二级：Redis（分布式缓存）**

当 Caffeine 里没有数据时，会去 Redis 查。Redis 的数据是全局共享的，无论请求落到哪台服务器，都能拿到同一份缓存数据。

### 3.3 具体的缓存读写逻辑

以查看用户详情（`getUserDetailMessage`）为例：

```java
userCaffeine.getAndAutoLoad(userId, stringRedisTemplate, redisKey, duration, UserDTO.class,
    key -> userMapper.getUserDetailMessage(userId));
```

这个方法做的事情是：
1. 先查 Caffeine，有就直接返回
2. Caffeine 没有，查 Redis，有就回填到 Caffeine 然后返回
3. Redis 也没有，查 MySQL（`userMapper.getUserDetailMessage`），查出来回填到 Redis 和 Caffeine，然后返回

这个"穿透加载"的逻辑封装在 `JvmCaffeineParent` 父类里，所有模块的 Caffeine 缓存都继承它，统一了行为。

### 3.4 一个有意思的细节：为什么查自己信息不走缓存

注意看 `getUserSelfMessage` 方法——它直接查 MySQL，跳过了缓存。

```java
public UserDTO getUserSelfMessage(){
    long userId = TokenIdContext.getAndCheck(...);
    return userMapper.getUserDetailMessage(userId);
}
```

这是故意的。因为用户刚改完自己的资料，缓存里还是旧数据，如果查缓存拿到的就是旧的，用户会感觉"我明明改了怎么没生效"——这就是缓存一致性问题。所以**查自己的信息直接走数据库，查别人的信息走缓存**，简单粗暴地解决了这个问题。

---

## 4. 头像上传的异步删除是怎么工作的

### 4.1 问题背景

用户上传新头像时，旧头像文件就没用了，需要删掉。但如果在上传请求里同步删除旧文件，会有两个问题：
- 文件删除是 IO 操作，可能很慢，拖慢接口响应
- 如果删除失败（比如文件被占用），会直接影响上传流程

所以需要**异步删除**：上传成功后再慢慢删旧文件，不阻塞用户。

### 4.2 上传头像的完整流程

`updateUserHeader` 方法做的事情：

1. **冷却期检查**：防止用户频繁换头像刷接口
2. **获取旧头像路径**：`userMapper.getHeaderPath(userId)` 查出当前的头像文件路径
3. **保存新文件**：`FileSave.quickCheckAndSaveFile` 把上传的图片保存到指定目录，校验文件大小和类型
4. **更新数据库**：`userMapper.updateUserHeader(userId, newAddr, oldAddr)` —— 注意这个 SQL 有一个巧妙的设计：

```sql
update user set header_image_addr=#{addr}
where user_id=#{userId} and is_delete=false
and header_image_addr = #{oldAddr}  -- 如果oldAddr不为null
-- 或者 header_image_addr IS NULL  -- 如果oldAddr为null
```

这个 SQL 带了 `oldAddr` 条件，相当于一个**乐观锁**：如果更新时数据库里的头像路径已经不是旧的了（说明被别的请求改过了），更新就会失败，返回 false。这时候新文件已经保存了但数据库没更新成功，所以要把刚保存的新文件也删掉。

5. **更新成功**：发一条 MQ 消息，告诉消费者去删除旧头像文件
6. **更新失败**：发一条 MQ 消息，告诉消费者去删除刚保存的新文件（因为数据库没更新成功，这个文件是废的）

### 4.3 MQ 消费者的删除逻辑

`DeleteFileUserConsumer` 监听 RabbitMQ 队列，收到文件路径后调用 `FileRemove.removeFileByPath` 删除文件。

但如果删除失败了呢？比如文件被其他进程锁定了？这时候消费者会做两件事：
- 记录错误日志
- 把文件路径写进 **Redis Stream**

### 4.4 Redis Stream 兜底：定时任务重试

`OldFileClearScheduled` 是一个定时任务，每 5 秒执行一次。它会从 Redis Stream 里读取之前删除失败的文件路径，重新尝试删除。

Redis Stream 在这里充当了一个"失败重试队列"的角色。为什么不用 RabbitMQ 的死信队列？因为 Redis Stream 更轻量，而且支持消费者组，可以多个消费者协同消费，不会重复处理。

整个流程可以总结为：

```
上传头像 → 保存新文件 → 更新DB → 发MQ消息 → 消费者收到 → 删除旧文件
                                              ↓ 删除失败
                                         写入Redis Stream → 定时任务每5秒重试
```

### 4.5 注销账号时的文件清理

用户注销账号时，也会发 MQ 消息删除头像文件。注意注销是**逻辑删除**（`is_delete=true`），不是物理删除，这样数据可以恢复，而且手机号会被改成 `{userId}删除{原手机号}`，释放手机号给其他人注册。

---

## 5. 安全设计：冷却期、验证码、密码错误限制

### 5.1 冷却期：所有敏感操作的保护伞

所谓"冷却期"就是"做完一次之后，要等一段时间才能再做下一次"。在 User 模块里，以下操作都有冷却期保护：

| 操作 | 冷却期目的 |
|------|-----------|
| 注册 | 防止同一个手机号短时间内反复注册 |
| 密码登录 | 防止暴力破解密码 |
| Token 刷新 | 防止频繁刷新 JWT |
| 修改密码 | 防止短时间内反复修改 |
| 修改头像 | 防止频繁上传文件 |
| 修改资料 | 防止频繁修改 |

冷却期的实现很简单：操作成功后，在 Redis 里设一个 Key（带过期时间），下次操作前先检查这个 Key 是否存在，存在就拒绝。

```java
RedisUtil.checkCooldown(stringRedisTemplate, key, duration);
```

### 5.2 验证码：防刷的第一道防线

所有敏感操作（注册、登录、修改密码、注销账号）都需要验证码。验证码的生成用了 `OPTUtil.generateOPTAndRecord`：生成 6 位随机数字，存到 Redis，设过期时间。

验证时用 `OPTUtil.checkOPT`：从 Redis 取出验证码，比对后删除（一次性使用）。

### 5.3 密码登录的"密码错误次数限制"

密码登录前会检查冷却期。这个冷却期的 Key 是 `{loginPasswordCooldown前缀}{手机号}`。虽然代码里没有显式的"连续错误 N 次才锁定"逻辑，但冷却期的存在本身就是一种限制：不管密码对不对，只要调了密码登录接口，就会进入冷却期，短时间内不能再试——这就从频率上限制了暴力破解。

### 5.4 其他安全细节

- **JWT Token 存在 Redis**：`commonRedisKeyConfig.getLoginToken().getRedisKey(userId)`，可以控制多设备登录数量（`maxStore`），超出后最早的 Token 会被踢掉
- **用户 ID 校验**：所有需要用户身份的操作，都通过 `TokenIdContext.getAndCheck` 从 Token 中解析出 userId，并校验 userId 是否在合法范围内（`userIdStart` 到 `userIdStart + idCapacity`），防止伪造 Token
- **手机号格式校验**：统一在 `CommonParamRulesConfig.phoneNumberCheck` 里做，保证了所有入口的手机号校验逻辑一致

---

## 6. 设计中的亮点和坑

### 6.1 亮点

**两级缓存设计**：Caffeine + Redis 的组合兼顾了性能和一致性。本地缓存纳秒级响应，Redis 保证多实例共享，整体设计思路清晰。

**异步删除旧文件 + Redis Stream 兜底**：用 MQ 异步处理文件删除，不阻塞主流程；失败了还有 Redis Stream 定时重试，保证最终一致性。这个模式在分布式系统里很常见，但实现得干净利落。

**乐观锁更新头像**：SQL 里带 `oldAddr` 条件，防止并发更新头像时出现问题。虽然不是一个典型的"版本号"乐观锁，但思路是一样的。

**查自己不走缓存**：一个小细节，但避免了一个很常见的"改完看不到效果"的体验问题。很多系统为了性能全局缓存，结果用户改完资料刷新页面还是旧数据，体验很差。

### 6.2 潜在坑点

**手机号唯一性完全依赖数据库唯一索引**：前面提到过，注册时业务层没有显式检查手机号是否已注册，完全靠数据库唯一索引兜底。这样做的好处是简单、不会有并发窗口，但缺点是：用户看到的错误信息可能是"数据库异常"而不是"该手机号已注册"，不够友好。如果要做改进，可以在业务层先查一次，查到已注册就返回友好提示，查不到再插入——虽然仍有并发窗口，但结合唯一索引就是双重保险。

**注册时没有设置冷却期 Key**：看 `registerUser` 方法，校验了冷却期（`checkCooldown`），但注册成功后没有看到设置冷却期 Key 的代码。这意味着冷却期只检查不设置？仔细看代码流程：`checkCooldown` 只是检查，真正的冷却期设置可能是在 `OPTUtil.checkOPT` 里验证码被消费后自然过期，或者依赖其他机制。这一点需要确认实际配置。

**OrderAmountConsumer 被注释掉了**：`OrderAmountConsumer` 类虽然存在，但 `@Component` 注解被注释了，说明它已经不生效。实际生效的是 `ChangeUserOrderAmountConsumer`，它监听的是 `Order_Exchange_Change_User_Order_Amount_Queue`，接收一个 `ChangeAmountDTO` 来更新订单数。这说明订单数统计从"简单累加"改成了"订单模块计算好变化量后通知"——更灵活，但需要订单模块配合。

**注销逻辑中 SQL 的写法**：`deleteUser` 的 SQL 是 `update user set is_delete=true and phone_number=concat(...)`，这个写法在 MySQL 里其实是 `SET is_delete = (true AND phone_number = concat(...))`，可能会被解析为布尔表达式。实际上应该是 `is_delete=true, phone_number=concat(...)`（用逗号分隔），这是一个潜在的 SQL 语法问题。

---

## 7. 快速启动

### 前置条件

启动 User 模块之前，确保以下服务已经就绪：
- **Nacos**（`127.0.0.1:8848`）：服务注册发现 + 配置中心
- **MySQL**：需要创建 `seek_food_user` 数据库，执行 `MysqlCreate.sql` 建表
- **Redis**：用于缓存、验证码、冷却期、Token 存储
- **RabbitMQ**：用于消息队列（注册通知、文件删除等）
- **Sentinel**：流量控制（可选，但已配置）

### 启动步骤

1. 在 Nacos 里确保所有配置已发布，特别是 `user-self-dev.yaml`（User 模块自身配置），包括 Redis Key 名称、冷却时间、Caffeine 参数等

2. 执行建表 SQL（`src/main/resources/MysqlCreate.sql`）

3. 启动 User 模块：
   ```bash
   cd User
   mvn spring-boot:run
   ```

4. 验证启动成功：访问 `http://localhost:10001`，或者查看 Nacos 控制台，服务列表里应该出现 `user` 服务

### 主要接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/register/opt?phoneNumber=xxx` | 获取注册验证码 |
| POST | `/register?phoneNumber=xxx&password=xxx&opt=xxx` | 注册 |
| GET | `/login/opt?phoneNumber=xxx` | 获取登录验证码 |
| GET | `/login?phoneNumber=xxx&opt=xxx` | 验证码登录 |
| GET | `/login/password?phoneNumber=xxx&password=xxx` | 密码登录 |
| GET | `/login/refresh` | 刷新 Token |
| GET | `/user/detail?userId=xxx` | 查看他人详细信息 |
| GET | `/user/self` | 查看自己的信息 |
| GET | `/user/password?phoneNumber=xxx` | 获取修改密码验证码 |
| PUT | `/user/password?phoneNumber=xxx&newPassword=xxx&opt=xxx` | 修改密码 |
| PUT | `/user/header` | 上传头像（MultipartFile） |
| PUT | `/user/message` | 修改个人资料 |
| GET | `/user/simple` | 批量获取用户简要信息 |
| GET | `/user/delete/opt` | 获取注销验证码 |
| DELETE | `/user/delete?opt=xxx` | 注销账号 |

### 技术栈

- Java 21
- Spring Boot 3.x / Spring Cloud
- MyBatis（数据访问）
- Nacos（服务发现 + 配置中心）
- Caffeine（JVM 本地缓存）
- Redis（分布式缓存 + 验证码 + 冷却期 + Redis Stream）
- RabbitMQ（消息队列）
- Sentinel（流量控制）
- Seata（分布式事务）
- SkyWalking（链路追踪）
- SpringDoc OpenAPI（API 文档）