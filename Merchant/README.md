# Merchant 模块 -- 商家服务

## 1. 这个模块是干什么的

Merchant 模块是整个 SeekFood 外卖平台的"商家端"核心服务。你可以把它理解成一个商家在平台上的"大管家"——它负责商家从注册、登录到日常运营的几乎所有事情。

具体来说，它管这些事情：

- **商家注册和登录**：手机号验证码登录、密码登录、Token 刷新，全都在这里
- **店铺资料管理**：店主姓名、身份证、个人照片、营业执照、门店展示照片、封面图、店铺简介、地址、经纬度
- **营业状态控制**：商家可以随时"开业"或"停业"，前端看到的状态就是从这来的
- **地理位置距离计算**：用户想知道离某个商家多远，就是这里算出来的
- **密码管理**：改密码、注销账户，也都走这个模块

这个模块跑在端口 **10002**，服务名就叫 **merchant**，注册在 Nacos 上，方便其他服务通过服务名找到它。

---

## 2. 商家注册开店的完整流程

一个商家从零到能在平台上被用户搜到，大致要经历这几个阶段：

### 第一步：获取验证码

商家在 App 上输入手机号，前端调用 `/register/opt` 接口。后端做了几件事：

1. 先校验手机号格式（规则由 Nacos 配置中心统一管理，不是硬编码的）
2. 生成一个 6 位数的随机验证码，存到 Redis 里，设置一个过期时间（比如 5 分钟）
3. 把验证码返回给前端（真实业务里应该发短信，这里做了模拟）

### 第二步：提交注册

商家填好验证码和密码，调用 `/register` 接口（POST）。后端会：

1. 再次校验手机号格式和密码强度
2. 用商家输入的验证码去 Redis 里比对，对不上就拒绝
3. 验证码通过后，用 Redis 的自增计数器生成一个唯一的商家 ID（不是数据库自增，是利用 Redis 的原子自增操作，保证分布式环境下 ID 唯一）
4. 把商家信息（手机号、密码）写入 MySQL 的 `merchant` 表
5. 同时在 Elasticsearch 里也创建一条商家文档，初始名字叫"商家 + ID"，方便后续搜索
6. 发一条 MQ 消息到资金模块，通知它给这个新商家初始化一个资金账户

> 为什么注册时要同时写 MySQL 和 ES？因为商家一注册就应该能被搜索到，哪怕信息还不完整。这是一种"先占坑"的策略。

### 第三步：完善店铺资料

注册完后的商家只有一个手机号和默认名字，接下来要逐步完善资料。这些操作都在 `/merchant` 路径下：

- **设置店主信息**（`PUT /merchant/master`）：上传店主姓名、身份证号、个人照片。注意，这个接口只能调用一次——因为设置了 BitMap 标记位，第二次调用会直接拒绝，防止重复设置。
- **上传营业执照**（`POST /merchant/proof`）：营业证明照片，可以上传多张（JSON 数组存储），有数量上限。
- **上传门店展示照片**（`POST /merchant/show`）：类似朋友圈那种展示图，同样是多张 JSON 存储。
- **设置封面图**（`PUT /merchant/home`）：店铺首页那张大图。
- **完善店铺信息**（`PUT /merchant/message`）：店名、简介、地址、经纬度。

### 第四步：开业

所有资料都填好后，调用 `/merchant/open` 切换营业状态。这个接口是个 toggle——开着的关了，关着的开了。状态变更后会同步到 ES，用户的搜索列表里就能看到（或看不到）这家店了。

### 关于"冷却期"机制

你会发现注册、登录、改资料、改密码、甚至是查询距离，几乎所有操作都有冷却期。这不是为了为难用户，而是出于安全考虑：

- 防止恶意刷验证码
- 防止暴力破解密码
- 防止高频修改资料导致数据不一致

冷却时间由 Nacos 配置中心动态下发，可以随时调整，不需要重启服务。

---

## 3. 距离计算是怎么实现的（Spatial4j 是什么，为什么用它）

### 问题背景

用户打开 App，看到一个商家列表，想知道"离我最近的那家店多远"。这个距离怎么算？

### 为什么不直接用数据库的 GIS 函数？

MySQL 确实有地理空间函数（比如 `ST_Distance_Sphere`），但不选它有几个原因：

1. **数据库耦合**：用了 GIS 函数就绑定了 MySQL，以后换数据库（比如 PostgreSQL 或 TiDB）很麻烦
2. **计算压力**：距离计算放在数据库里，每次查询都要算，高并发时数据库扛不住
3. **不够灵活**：有些场景需要在 Java 代码层做二次计算（比如排序、筛选），光靠数据库不够

### Spatial4j 是什么

Spatial4j 是一个轻量级的纯 Java 空间计算库，完全不依赖数据库。它支持：

- 地球球面距离计算（基于经纬度）
- 多种地球模型（球体、椭球体等）
- 多边形、矩形等空间形状判断

在这个模块里，我们用 `SpatialContext.GEO` 作为地球模型，距离计算的核心代码在 `MerchantServiceImpl.getDistance()` 里。

### 计算流程

当用户想知道离某个商家多远时，调用 `/merchant/distance` 接口：

1. 前端传来用户的经纬度（lon, lat）和目标商家 ID
2. 后端从 MySQL 查出商家的经纬度
3. 用 Spatial4j 的 `pointLatLon()` 把用户坐标和商家坐标分别转成 Point 对象
4. 调用 `ctx.calcDistance(userPoint, merchantPoint)` 算出两个点之间的角度距离
5. 把角度距离乘以 `DistanceUtils.DEG_TO_KM`（一个常量，约 111.32）再乘以 1000，就得到了以**米**为单位的直线距离

```java
// 核心代码就这么几行
Point userPoint = ctx.getShapeFactory().pointLatLon(lat, lon);
Point merchantPoint = ctx.getShapeFactory().pointLatLon(simplePoint.getLat(), simplePoint.getLon());
return (long) (ctx.calcDistance(userPoint, merchantPoint) * DistanceUtils.DEG_TO_KM * 1000);
```

### 注意事项

这个距离是**直线距离**（球面大圆距离），不是实际的行车距离。对于外卖业务来说，直线距离已经足够做粗略筛选，精确的导航距离应该交给地图 SDK（比如高德或百度地图）。

---

## 4. 多图片管理：营业执照、门店照片怎么存

### 数据存储方式

营业执照（`merchant_proof_image_addr`）和门店展示照片（`merchant_show_image_addr`）在数据库里用的是 **JSON 类型**字段。也就是说，一个字段里存的是一个 JSON 数组，比如：

```json
["proof_20260730_001.jpg", "proof_20260730_002.jpg"]
```

这样做的好处是：
- 不需要单独的图片表，也不用 JOIN 查询
- 图片的顺序天然就是数组的顺序
- 前端拿到 JSON 直接解析就能用

### 图片操作

每一类图片都支持三种操作：**增加**、**删除**、**替换**。

以营业执照为例：

- **增加**（`POST /merchant/proof`）：上传文件，后端检查文件大小和类型，保存到指定目录，然后在 MySQL 的 JSON 数组里追加一条。如果数量超过上限（由配置决定），MySQL 的 UPDATE 语句会判断并拒绝，同时后端会发 MQ 消息把刚保存的文件删掉。
- **删除**（`DELETE /merchant/proof?index=0`）：指定数组下标，后端先查出对应的旧文件路径，然后更新 MySQL 把那一项 JSON 去掉，最后发 MQ 消息删除旧文件。
- **替换**（`PUT /merchant/proof`）：上传新文件，保存后替换数组里指定位置的旧值，同时 MQ 删除旧文件。

### 文件删除的不丢不重机制

你可能注意到了，修改或删除图片时，旧文件不是直接 `File.delete()` 的，而是发一条 MQ 消息到专门的"文件删除队列"。由 `DeleteFileMerchantConsumer` 这个消费者来处理实际删除。

为什么这么做？

1. **异步化**：文件删除可能很慢（磁盘 IO），不应该阻塞主流程
2. **容错性**：如果删除失败（比如文件被占用），MQ 会自动重试
3. **兜底机制**：MQ 重试也失败的话，会把失败的文件路径写入 Redis Stream，由 `OldFileClearScheduled` 定时任务每隔 5 秒重新尝试删除

这样就形成了一个"三重保障"：先正常删，失败了 MQ 重试，再失败定时任务兜底。

### 图片上传的校验

每次上传图片，都会经过 `FileSave.quickCheckAndSaveFile()` 的快速校验：
- 文件大小不能超过配置上限
- 文件类型必须是允许的图片格式（jpg、png 等）
- 校验通过后复制到目标目录，返回相对路径存库

---

## 5. 和 ES 的同步：为什么商家信息要同步到 ES

### 为什么需要双写

MySQL 是"真相之源"（Source of Truth），所有数据都存在这里。但 MySQL 不擅长做全文搜索和地理空间搜索——你不可能让用户输入"麻辣烫"后去 MySQL 里 LIKE 模糊查询，那太慢了。

ES（Elasticsearch）正好擅长这些：
- 分词搜索（用户在搜索框输入"火锅"，能搜到"重庆火锅"、"潮汕牛肉火锅"）
- 地理距离筛选（搜"附近 3 公里内的商家"）
- 随机排序（Feed 流推送，每次刷新都不一样）

所以商家信息需要同时存在于 MySQL 和 ES 中：MySQL 负责可靠的增删改查，ES 负责高效的搜索和推送。

### 同步机制

同步不是实时的，而是**准实时**的。整个链路是这样的：

1. **触发同步**：当商家信息变更（改店名、改地址、切换营业状态等），变更操作完成后，调用 `merchantUtil.esSync(merchantId)` 方法
2. **BitMap 去重**：这个方法会先用 Redis 的 BitMap 检查这个商家 ID 是否已经在"待同步"状态。如果已经在同步队列里了，就不重复添加——这是一个轻量级的去重手段
3. **写入 Redis Stream**：如果不在队列中，就设置 BitMap 标记位，然后往 Redis Stream 里写入一条消息（包含商家 ID）
4. **定时投递到 MQ**：`esSyncScheduled` 这个定时任务（每 5 秒执行一次），从 Redis Stream 里批量读取消息，逐个投递到 RabbitMQ 的 ES 同步队列
5. **MQ 消费者处理**：`EsSyncMerchantConsumer` 接收到消息后，从 MySQL 查出最新的商家信息，组装成 `MerchantEsDTO`，调用 ES 的 `update` 接口（注意是 update 不是 save，文档不存在不写入，防止注册时先写 ES 的时序问题）
6. **清除标记**：无论同步成功还是失败，都会清除 BitMap 标记位。如果失败会抛异常让 MQ 重试

### 为什么不用 ES 的 CDC 方案

业界还有一种方案是通过 Canal 监听 MySQL 的 Binlog 来同步 ES，但这里选择了应用层双写。原因：
- 减少了组件依赖（不需要额外部署 Canal）
- 同步时机更可控（只在需要的时候同步，不是每条 SQL 变更都同步）
- 配合 BitMap 去重，避免高频变更时的重复同步

### ES 的索引结构

ES 里商家文档的 `mappings` 在 `EsCreate.txt` 里定义。比较关键的几个字段：
- `merchant_name`：用 ngram 分词器，最小 1 个字符、最大 2 个字符切词。这样用户输入"火锅"能匹配到"火"和"锅"两个 token，搜索体验更好
- `merchant_location`：`geo_point` 类型，用来做地理距离筛选
- `is_open` 和 `is_delete`：布尔字段，用来过滤营业状态和已删除的商家

---

## 6. 设计中的亮点和坑

### 亮点

**1. Caffeine + Redis 两级缓存**

商家详情查询（`getMerchantDetail`）走了 Caffeine 本地缓存 + Redis 缓存的模式。Caffeine 是 JVM 内存缓存，速度极快；Redis 是分布式缓存，多实例共享。查询时先查 Caffeine，没有再查 Redis，最后才查 MySQL。数据变更时，同时删除 Caffeine 和 Redis 中的缓存。

**2. BitMap 去重 + 冷却期双重防护**

高并发场景下，防止重复操作靠两样东西：BitMap 做状态标记（比如"店主信息已设置"），冷却期做频率限制（比如"距离查询 5 秒内只能查一次"）。BitMap 占内存极小，一个 bit 就能标记一个商家的状态。

**3. 配置全外置**

所有参数——冷却时间、图片大小上限、ES 索引名、路由 Key——全部放在 Nacos 配置中心。改配置不需要重启服务，Nacos 推送后自动生效（`@RefreshScope`）。

**4. 注册时 ID 用 Redis 自增生成**

不用数据库自增 ID，而是用 Redis 的 `INCR` 命令生成。好处是分布式环境下多个服务实例不会产生 ID 冲突，而且 ID 生成速度比数据库快得多。

**5. ES 搜索的 Search After 分页**

Feed 流和搜索接口都支持 `docScore` 和 `docId` 参数做深度分页（Search After），而不是传统的 `from+size`。这避免了 ES 深度分页的性能问题，同时配合随机种子（seed）保证每次刷新的结果稳定。

**6. 文件删除的"三重保障"**

前文已经说过：正常删除 -> MQ 重试 -> 定时任务兜底。这种设计确保了即使文件系统偶尔出问题，垃圾文件也不会永久残留。

### 潜在的坑

**1. 距离计算是直线距离，不是实际距离**

球面距离跟实际的行车距离可能差很多，特别是城市里有河流、高架桥的时候。如果后续要做"配送距离预估"，建议接地图 SDK 的路线规划 API，而不仅仅是 Spatial4j。

**2. ES 同步是异步的，会有短暂不一致**

从商家修改信息到 ES 数据更新，中间有 Redis Stream 定时投递（5 秒）+ MQ 消费的延迟。这意味着用户可能在短时间内看到旧数据。对于外卖业务来说，这个延迟是可以接受的，但如果你在做一个对实时性要求极高的场景（比如股票交易），就要考虑同步方案了。

**3. JSON 字段内的图片查询不便**

营业执照和展示照片用 JSON 数组存，好处是结构简单，坏处是如果你想查"某张图片被哪些商家使用"，MySQL 的 JSON 查询比较麻烦，而且没法建普通索引。如果后续有这种需求，可能需要考虑拆成单独的图片表。

**4. 冷却期可能导致用户体验不佳**

如果冷却时间设置得太长，用户可能会觉得"为什么我刚改完就不能再改了"。这需要产品同学根据实际场景调优 Nacos 里的冷却时间参数。

**5. 注册时用的手机号验证码是模拟的**

从代码看，验证码是直接返回给前端的（`getRegisterOpt` 返回了验证码），真实业务里应该通过短信发送，验证码不应该暴露给客户端。这是教学/演示项目的简化处理。

---

## 7. 快速启动

### 前置依赖

在启动 Merchant 模块之前，确保以下服务已经就绪：

| 依赖 | 用途 | 默认地址 |
|------|------|----------|
| Nacos | 服务注册发现 + 配置中心 | 127.0.0.1:8848 |
| MySQL | 商家数据存储 | 由 Nacos 配置文件指定 |
| Redis | 缓存、验证码、BitMap、Stream | 由 Nacos 配置文件指定 |
| Elasticsearch | 商家搜索 | 由 Nacos 配置文件指定 |
| RabbitMQ | 消息队列（ES 同步、文件删除等） | 由 Nacos 配置文件指定 |

### 数据库初始化

执行 `src/main/resources/MysqlCreate.sql` 创建数据库和表：

```sql
CREATE DATABASE if not exists seek_food_merchant;
-- 然后执行建表语句
```

### ES 索引初始化

参考 `src/main/resources/EsCreate.txt` 创建 ES 索引：

```bash
curl -X PUT "http://localhost:9200/merchant" -H "Content-Type: application/json" -d '{...}'
```

### 启动服务

1. 确保父项目 `SeekFood` 的依赖已经安装（`Config`、`Util`、`DTO` 三个模块）
2. 在 IDE 中直接运行 `MerchantApplication.java` 的 main 方法
3. 或者用 Maven 命令行：

```bash
cd Merchant
mvn spring-boot:run
```

### 验证启动

服务启动后，可以通过以下方式验证：

- Nacos 控制台查看 `merchant` 服务是否注册成功
- 访问 Swagger 文档：`http://localhost:10002/swagger-ui.html`（如果配置了 SpringDoc）
- 注册接口测试：`GET http://localhost:10002/register/opt?phoneNumber=13800138000`

### 关键配置说明

所有可调参数都在 Nacos 上，属于 `merchant-self-dev.yaml` 配置文件（分组 `SEEK_FOOD_GROUP`）。以下是几个常用配置项的作用：

- `merchant-register-opt`：注册验证码的过期时间
- `merchant-login-password-cooldown`：密码登录失败后的冷却时间
- `merchant-update-message-cooldown`：修改店铺信息的冷却时间
- `proof-image-number-max`：营业执照最多上传几张
- `show-image-number-max`：门店展示图最多上传几张
- `image-size` 和 `image-type`：上传图片的大小和格式限制

---

## 模块依赖关系

```
Merchant 模块
  ├── Config 模块（公共配置：JWT、Redis Key、MQ 绑定、Sentinel 规则等）
  ├── Util 模块（工具类：Token、Redis、文件操作、MQ、验证码等）
  └── DTO 模块（数据传输对象：MerchantDTO、MerchantEsDTO）
```

外部依赖（通过 Maven 引入）：
- `spring-boot-starter-web`：Spring Boot Web 框架
- `mybatis-spring-boot-starter`：MyBatis 数据库访问
- `spring-cloud-starter-alibaba-nacos-discovery`：Nacos 服务注册发现
- `spring-cloud-starter-alibaba-nacos-config`：Nacos 配置中心
- `spring-cloud-starter-alibaba-sentinel`：Sentinel 流量控制
- `spatial4j`：空间距离计算
- `springdoc-openapi`：Swagger 接口文档
- `spring-cloud-starter-alibaba-seata`：分布式事务（Seata）
- `apm-toolkit-logback`：SkyWalking 链路追踪日志

---

## 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/register/opt` | 获取注册验证码 |
| POST | `/register` | 提交注册 |
| GET | `/login/opt` | 获取登录验证码 |
| GET | `/login` | 验证码登录 |
| GET | `/login/password` | 密码登录 |
| GET | `/login/refresh` | 刷新 Token |
| GET | `/merchant/detail` | 查询商家详情（公开） |
| GET | `/merchant/self` | 查询自己的详情 |
| PUT | `/merchant/master` | 设置店主信息 |
| POST | `/merchant/proof` | 添加营业执照 |
| DELETE | `/merchant/proof` | 删除营业执照 |
| PUT | `/merchant/proof` | 替换营业执照 |
| POST | `/merchant/show` | 添加展示图 |
| DELETE | `/merchant/show` | 删除展示图 |
| PUT | `/merchant/show` | 替换展示图 |
| PUT | `/merchant/home` | 更换封面图 |
| PUT | `/merchant/message` | 修改店铺信息 |
| GET | `/merchant/password/opt` | 获取改密验证码 |
| PUT | `/merchant/password` | 修改密码 |
| GET | `/merchant/delete/opt` | 获取注销验证码 |
| DELETE | `/merchant/delete` | 注销账户 |
| PUT | `/merchant/open` | 切换营业状态 |
| GET | `/merchant/distance` | 计算与商家距离 |
| GET | `/search/feed` | Feed 流随机推送商家 |
| GET | `/search` | 按名称搜索商家 |