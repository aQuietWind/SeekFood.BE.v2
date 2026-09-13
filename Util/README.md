# Util（工具库）

> 非服务模块（没有端口），所有服务的公共工具库

## 1. 这个模块是干什么的？

Util 模块是整个 SeekFood 项目的**"工具箱"**。你写业务代码时会发现很多操作是重复的——生成 Token、发 MQ 消息、检查冷却期、生成验证码、保存文件……如果每个服务都自己写一遍，不仅浪费时间，还容易写出 bug。

Util 模块把这些通用操作封装成了**一行调用的静态方法**，业务模块直接拿来用就行。比如你想发一条 MQ 消息，只需要 `MQUtil.send(exchange, routingKey, message, rabbitTemplate)` 一行代码，不用关心消息怎么序列化、怎么确认投递成功。

## 2. 最核心的几类工具

### JWT 工具（JWTUtil / TokenUtil）

这是整个系统认证体系的基石。它的核心功能是"生成 Token"和"验证 Token"。

**为什么有多套密钥？** 系统里有四种角色：普通用户、商家、骑手、管理员。如果所有人用同一把密钥，那一个用户的 Token 泄露了，理论上可以被伪造用来访问其他角色的接口。所以每种角色用不同的密钥签发 Token，密钥之间存在隔离。

**Token 是怎么识别角色的？** 生成的 Token 格式是 `{角色标识}__{JWT本体}`，比如 `1__eyJhbG...`。验证时，先按 `__` 分割，前面的 `1` 告诉系统"这是用户 Token"，然后从 Nacos 配置里找到对应的密钥来验签。这样同一个验证入口可以处理所有角色的 Token。

**Token 存在 Redis 里做什么？** `TokenUtil.getAndRecordToken()` 会把 Token 存入 Redis 的 ZSet 有序集合，支持单设备多 Token 登录（比如手机和电脑同时登录）。Token 数量超过上限时，Lua 脚本原子性地淘汰最早的 Token，不需要业务代码加锁。

### MQ 工具（MQUtil）

**为什么封装 MQUtil？** 直接用 Spring AMQP 的 RabbitTemplate 发消息需要写很多样板代码：创建 CorrelationData、设置回调、处理异常。MQUtil 把这些封装成一行调用。

**延时消息是怎么实现的？** `sendWithTLL()` 发送消息时设置一个 TTL（存活时间），消息过期后自动转入死信队列，死信队列的消费者再处理。比如"下单后 30 分钟未支付自动取消"，就是发一条 30 分钟 TTL 的消息，过期后死信消费者执行取消逻辑。

**仲裁队列（Quorum Queue）和普通队列有什么区别？** 仲裁队列基于 Raft 协议，数据在多个节点间强一致复制，比老式的镜像队列更可靠。`generateQuorumQueue()` 一行代码创建仲裁队列。

### Redis 工具（RedisUtil）

Redis 在这个项目里远不止是缓存，它至少干了 5 件事：

**冷却期（checkCooldown）：** 用 `SETNX` 原子操作实现。`SETNX` 的意思是"如果 Key 不存在就设置，存在就什么都不做"。利用这个特性，第一次请求时设置一个 Key（带 TTL），第二次请求时 Key 还在，就直接拒绝。因为 `SETNX` 是原子的，不存在"检查"和"设置"之间的并发问题。

**Stream 消费（readStreamAndHandle）：** 封装了"阻塞读取 -> 业务处理 -> ACK 确认"的完整流程。循环读取直到队列清空，配合多消费者实例实现负载均衡。主要用于文件删除失败后的重试。

**BitMap 分区（oftenSetBit）：** 用 BitMap 存储"是否收藏""是否点赞"等布尔状态，比用 Set 集合节省大量内存。亿级 ID 范围按 `areaNumber` 分区存储，避免单个 Key 过大。

### 多级缓存（JvmCaffeineParent）

这是缓存策略的核心实现。`JvmCaffeineParent` 是一个抽象父类，业务模块继承它就能拥有三级缓存能力。

**为什么是三级？** 查询速度：Caffeine（微秒级） > Redis（毫秒级） > MySQL（10毫秒级）。热点数据放在 Caffeine 里，次热点在 Redis 里，冷数据在 MySQL 里。查数据时先用 Caffeine，没命中再用 Redis，再没有才查数据库。

**缓存一致性怎么保证？** 更新数据时，调用 `updateAndRemoveCaffeine()`，先删 Redis 缓存，再删 Caffeine 缓存。为什么要先删 Redis 再删本地？因为 Redis 是分布式的，其他实例可能也有这个数据的缓存，先删 Redis 能尽快让其他实例感知到数据变化。

**怎么防止缓存穿透？** 查数据库也查不到的数据，缓存一个特殊标记 `"n"`，下次再查就直接返回 null，不会反复查数据库。

### 异常体系（BizException / ErrorCodeEnum）

**为什么不用 Spring 自带的异常？** 因为需要统一的错误码。`ErrorCodeEnum` 里每个错误码都有三个信息：**业务码（前端用来分支处理）、HTTP 状态码、中文提示**。比如 `REQUEST_IN_COOLDOWN` 的业务码是 14406，HTTP 状态码是 429，提示是"请求过于频繁"。

所有工具类内部抛出的都是 `BizException`，配合 Config 模块的全局异常处理器，统一返回 `Result` 格式。

## 3. 为什么用函数式接口（RunFunction 等）？

你可能会问：Java 不是已经有 `Runnable`、`Consumer`、`BiConsumer` 了吗？为什么还要自己定义 `RunFunction`、`RunWithParam`、`RunWithTwoParams`？

原因是**语义更清晰**。`Runnable` 通常用于线程相关场景，`Consumer` 暗示"消费数据并可能产生副作用"，而 `RunFunction` 明确表示"这是一个回调函数，执行一段逻辑，不需要返回值"。在工具类内部，用 `RunFunction` 作为回调参数，读代码的人一眼就知道"这里传的是一个要执行的逻辑片段"。

另一个原因是**可以抛异常**。Java 标准函数式接口的方法签名不带 `throws`，如果回调里需要抛 `BizException`，就得在 lambda 里 try-catch 包装。而自定义的 `RunFunction` 等接口直接声明了 `throws Exception`，用起来更顺手。

## 4. 亮点和坑

**亮点：**

- **多密钥 Token 路由**：通过 `headerSign` 前缀识别角色，一个方法搞定所有角色的 Token 验证。
- **Lua 脚本原子操作**：Token 池管理用 Lua 脚本在 Redis 端原子执行，避免了多次 Redis 命令之间的并发问题。
- **三级缓存自动加载**：`getAndAutoLoad()` 一个方法完成 Caffeine -> Redis -> MySQL 的逐级回源和回填。
- **BitMap 分区设计**：亿级 ID 范围按区分片存储，既节省内存又不影响性能。

**需要注意的坑：**

- **ThreadLocal 必须清理**：`TokenIdContext` 在请求结束后必须调用 `remove()`，否则 ThreadLocal 会泄漏。线程池复用线程时，下一次请求可能读到上一次残留的 ID。
- **Caffeine 初始化**：继承 `JvmCaffeineParent` 时必须在构造器里初始化 `CACHE` 字段，否则 NPE。
- **文件路径安全**：`FileRemove` 内部用 `Path.get()` 拼接路径，不要自己用字符串拼接，防止路径遍历攻击。
- **Lua 脚本路径**：`luaQuickInit("lua/token_add.lua")` 的路径是 ClassPath 相对路径，要确保脚本文件在 `resources/lua/` 目录下。

## 5. 快速开始

**引入依赖：**
```xml
<dependency>
    <groupId>com.seek.food</groupId>
    <artifactId>Util</artifactId>
</dependency>
```

**常用操作速查：**
```java
// 生成 JWT Token
String token = JWTUtil.obtainJwt(userId, secretKey, expireMillis);

// 发送 MQ 消息
MQUtil.send("exchange", "routingKey", dto, rabbitTemplate);

// 冷却期检查（命中直接抛异常）
RedisUtil.checkCooldown(redis, "cooldown:user:123", 5);

// 三级缓存查询
userCache.getAndAutoLoad(userId, redis, "user:" + userId, 1800, UserDTO.class, userService::getById);

// 生成验证码
String code = OPTUtil.generateOPTAndRecord(redis, "opt:login:13800138000", 300, 6);

// 保存文件
String fileName = FileSave.quickCheckAndSaveFile(file, "/uploads", 5*1024*1024, Set.of(".jpg", ".png"));

// 抛出业务异常
throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
```