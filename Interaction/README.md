# Interaction（互动服务）

> 端口：10012 | 服务名：interaction | 依赖：Config、Util、DTO

---

## 1. 这个模块是干什么的

Interaction 模块是整个 SeekFood 平台的**用户行为记录中心**。它只做两件事，但这两件事几乎贯穿了所有业务场景：

**收藏商家**：用户看到一个不错的商家，点一下收藏，下次就能在"我的收藏"里快速找到它。这个操作看起来简单，但背后涉及"谁收藏了谁"、"收藏数是多少"、"收藏列表怎么查"等一系列问题。

**点赞内容**：用户可以对商家、一级评论、二级评论点赞。点赞和收藏的逻辑几乎一模一样，所以放在同一个模块里处理。

你可能觉得奇怪：为什么收藏和点赞要单独开一个模块？直接放在 Merchant 或 Comment 模块里不好吗？原因有两个：

第一，**解耦**。收藏和点赞是高并发操作，如果跟商家或评论的业务逻辑绑在一起，任何一个模块出问题都会互相影响。独立出来后，Interaction 挂了不影响查看商家和评论，只是点赞和收藏功能暂时不可用。

第二，**统一管理**。商家点赞、评论点赞、收藏商家，本质上都是"用户 A 对目标 B 执行了一个布尔操作（true/false）"。把它们抽象成同一套逻辑，代码复用率高，维护成本低。

---

## 2. BitMap 是什么，为什么用它存收藏/点赞状态

这个问题是整个 Interaction 模块最核心的设计决策，值得认真讲。

**先说传统做法：用数据库存。** 每次用户点赞，就在 `like_connection` 表里插入一条记录 `{userId: 123, aimId: 456, isLike: true}`。用户取消点赞，就更新这条记录为 `false`。查用户是否点赞，就 `SELECT * FROM like_connection WHERE userId=123 AND aimId=456`。

这个做法在小数据量时完全没问题。但想象一下：如果平台有 100 万用户，每个用户平均点赞 50 次，那就是 5000 万条记录。每次用户打开一个商家页面，都要查一次"我有没有给这个商家点赞"，5000 万条记录里做索引查询，即使有索引，并发一大还是很慢。

**Redis BitMap 的做法：** BitMap 是 Redis 提供的一种特殊数据结构，它不是存"键值对"，而是操作**二进制位**。你可以把它想象成一个很长的 01 串，每一位代表一个状态。

具体的用法是这样的：

1. 给每个用户分配一个 BitMap，Key 是 `like:merchant:{userId}`。
2. 这个 BitMap 的长度等于系统中最大的商家 ID（比如 100 万）。
3. 当用户要给商家 ID 为 456 点赞时，就把这个 BitMap 的第 456 位设为 1。
4. 要检查用户是否点赞了商家 456，就读取第 456 位，是 1 表示已点赞，是 0 表示没点赞。

**用 BitMap 有什么好处？**

- **极快**。设置和读取一个 bit 位的时间复杂度是 O(1)，比数据库的索引查询快几个数量级。
- **极省内存**。一个用户的一个 BitMap，如果长度是 100 万，只占 100 万 bit = 125 KB。100 万用户全量加载也才 125 GB，实际上 Redis 会对稀疏 BitMap 做压缩，实际占用远小于这个值。
- **天然支持批量查询**。比如"查用户收藏了哪些商家"，可以用 `BITFIELD` 命令一次性读取多个 bit，比数据库的 `WHERE userId=xxx` 快得多。

**但 BitMap 也有局限：** 它只能存布尔值（是/否），不能存"什么时候点赞的"、"点赞了几次"这些附加信息。所以 Interaction 模块的做法是：**Redis BitMap 存状态（当前是否已点赞/收藏），MySQL 存详细记录（谁、什么时候、对什么）。** 两者各有分工，BitMap 保证查询速度，MySQL 保证数据持久化和可审计。

**代码里的细节：** `RedisUtil.oftenSetBitWithPerX` 这个方法名里的 `PerX` 指的是"分区"。如果 BitMap 太长（比如 ID 范围是 1000 万），直接操作一个 1000 万位的 BitMap 效率不高，所以按每 X 位分成多个小 BitMap，通过 `aimId / X` 确定用哪个 BitMap，`aimId % X` 确定具体是哪一位。这个 X 的值由 Nacos 配置中心的 `bitmapPerXNumber` 控制，可以动态调整。

---

## 3. 收藏和点赞的完整流程

收藏和点赞的逻辑几乎一模一样，这里以**收藏商家**为例，走一遍完整流程。

**第一步：用户点击"收藏"按钮。** 前端调用 `PUT /interaction/collect/merchant?merchantId=456&value=true`。注意，这里是 `PUT` 而不是 `POST`，因为收藏是一个幂等操作 —— 收藏两次等于收藏一次，取消收藏两次等于取消收藏一次。

**第二步：操作 Redis BitMap。** `CollectServiceImpl.collectMerchant` 方法被调用，它先校验 `merchantId` 是否合法，然后调用 `quickSetBitMap` 方法。这个方法内部调用 `RedisUtil.oftenSetBitWithPerX`，把用户 BitMap 中对应商家 ID 的那一位设为 `true`（或 `false`，取决于 `value` 参数）。

重点来了：`oftenSetBitWithPerX` 方法会**先读取当前位的值，再设置新值，然后返回"是否发生了变化"**。如果用户之前已经收藏了，现在又点收藏，位的值没变，返回 `false`；如果之前没收藏，现在收藏了，返回 `true`。这个返回值很重要，因为它决定了后续要不要发 MQ 消息。

**第三步：发 MQ 消息。** 如果 BitMap 的值确实发生了变化（返回 `true`），就发两条 MQ 消息：

- 一条到 `ChangeMerchantCollectAmountQueue`，内容是 `{merchantId: 456, changeNumber: +1}`（或 -1）。这条消息会被 Merchant 服务消费，更新商家的收藏数。
- 一条到 `SyncCollectStateQueue`，内容是 `{aimId: 456, accountId: 123, value: true, type: 0}`。这条消息会被 Interaction 自己的消费者 `SyncCollectStateConsumer` 消费，把状态同步到 MySQL 的 `collect_connection` 表。

**第四步：MySQL 持久化。** `SyncCollectStateConsumer` 收到消息后，调用 `collectMapper.syncCollect`，执行一条 `INSERT ... ON DUPLICATE KEY UPDATE` 语句。如果 `(aimId, accountId)` 这个组合已经存在，就更新 `is_collect` 字段；如果不存在，就插入新记录。这个操作是幂等的，重复消费消息不会出错。

**第五步：查询收藏状态。** 当用户打开商家页面时，前端调用 `GET /interaction/collect/merchant?merchantId=456`。后端直接读 Redis BitMap 对应位，返回 `true` 或 `false`。不查 MySQL，因为 MySQL 只是备份，Redis 才是"实时状态"。

**查询收藏列表**稍微复杂一点：`GET /interaction/collect/merchant/list?start=0&need=20`。这个接口直接查 MySQL 的 `collect_connection` 表，因为要做分页，BitMap 虽然支持批量读取，但处理分页不太方便。不过因为收藏列表查询频率远低于单个状态查询，MySQL 完全扛得住。

**点赞的流程跟收藏一模一样，只是多了一个 `type` 字段来区分目标类型：**
- `type = 0`：点赞商家
- `type = 1`：点赞一级评论
- `type = 2`：点赞二级评论

每种类型对应不同的 BitMap Key（比如 `like:firstComment:{userId}`）和不同的 MySQL 记录。

---

## 4. 亮点和坑

**亮点：**

- **Redis BitMap 做状态存储，MySQL 做持久化，各司其职。** 这是典型的"读写分离"思路。99% 的请求是"查状态"（只读 Redis），只有状态变更时才写 MySQL。Redis 扛住了大部分流量，MySQL 负责数据安全和审计。

- **一句话点赞/取消点赞，同一接口。** 前端传 `value=true` 就是点赞，`value=false` 就是取消。后端不需要维护两个接口，逻辑简洁。而且通过 BitMap 的"变更检测"机制，只有真正的状态变化才触发 MQ 消息，避免无意义的数据库写入。

- **MQ 双向同步。** Interaction 通过 MQ 通知 Merchant 服务更新收藏数/点赞数，同时也通过 MQ 通知 Comment 服务更新评论点赞数。这种"广播"模式让 Interaction 不需要知道下游有哪些服务，新增一个下游只需要多监听一个队列即可。

- **分区 BitMap（PerX）设计。** 如果直接用一个巨大的 BitMap，当 ID 范围很大时，Redis 操作大 BitMap 的性能会下降。通过分区（每 X 位一个 BitMap），每个 BitMap 的大小可控，操作效率稳定。

**坑：**

- **Redis 和 MySQL 的数据可能短暂不一致。** 用户在 BitMap 里已经取消收藏了，但 MySQL 里的同步消息还在队列里排队，这时候如果有人查 MySQL（比如收藏列表），会看到旧数据。不过考虑到收藏列表查询用的是 MySQL，单个状态查询用的是 Redis，而且 MQ 消费延迟通常只有几毫秒，这个不一致窗口非常短，对用户体验影响极小。

- **BitMap 的大小依赖 ID 范围。** 如果商家 ID 是全局递增的，而且 ID 范围很大（比如从 1 到 1 亿），即使做了分区，每个分区的 BitMap 仍然可能很大。好在 SeekFood 的 ID 设计是按角色分段（用户 ID 从一个基数开始，商家 ID 从另一个基数开始），每个分段的 ID 范围是可控的。

- **收藏列表查询走 MySQL，没有缓存。** 如果用户频繁刷新收藏列表，每次都会打到数据库。不过这个场景频率不高（用户不会一直刷新收藏列表），而且有分页限制，暂时不需要加缓存。如果未来需要优化，可以在 Redis 里维护一个用户的收藏商家 ID 列表（用 Set 或 ZSet），BitMap 只用来快速判断"是否收藏"。

---

## 5. 快速启动

**前置条件：**
- MySQL 8.0+（执行 `src/main/resources/MysqlCreate.sql` 建库建表）
- Redis 7.0+
- RabbitMQ 3.9+
- Nacos 2.0+（需要配置好 namespace、group 和相关的配置文件）

**启动步骤：**

1. 确保 Config、Util、DTO 三个公共模块已经编译安装到本地 Maven 仓库。
2. 确保 Nacos 上已经配置好了 `interaction-self-dev.yaml`、`mq-name-bind-dev.yaml`、`common-param-rules-dev.yaml` 等配置文件。
3. 启动 Interaction 服务的主类 `InteractionApplication.java`。
4. 启动后，服务会注册到 Nacos，端口为 10012。

**验证：**

- 调用 `PUT /interaction/collect/merchant?merchantId=456&value=true` 收藏一个商家，检查 Redis 中对应 BitMap 的位是否被设为 1（可以用 `redis-cli` 执行 `GETBIT` 命令）。
- 检查 RabbitMQ 管理界面，确认 `SyncCollectStateQueue` 和 `ChangeMerchantCollectAmountQueue` 收到了消息。
- 检查 MySQL 的 `collect_connection` 表，确认同步记录已写入。
- 调用 `GET /interaction/collect/merchant?merchantId=456` 查看收藏状态，应返回 `true`。
- 调用 `PUT /interaction/like/comment/first?commentId=789&value=true` 点赞一条一级评论，检查 Comment 服务的 `first_comment` 表中对应记录的 `like_amount` 是否自增了（通过 MQ 异步更新，可能需要等几秒）。