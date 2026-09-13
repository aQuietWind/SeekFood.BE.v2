# Comment（评论服务）

> 端口：10011 | 服务名：comment | 依赖：Config、Util、DTO

---

## 1. 这个模块是干什么的

Comment 模块负责 SeekFood 平台的**评价体系**。你在外卖 App 上看到的"用户评价"和"商家回复"，就是由这个模块管着的。

具体来说，它做三件事：

第一，**管理一级评论**。用户吃完一顿饭后，可以对订单写评价，这个评价就是"一级评论"。带上文字、图片，给商家打分（虽然当前版本打分是隐式的，通过点赞数体现）。

第二，**管理二级回复**。别人看到一条评论后，可以在下面跟帖回复，这就是"二级评论"（也叫子评论）。商家也可以在自己的评论下面回复用户，这是"商家回复"。

第三，**协助点赞系统**。虽然点赞的具体操作由 Interaction 模块处理，但点赞数量的变更需要同步到 Comment 模块 —— 比如用户给某条评论点了赞，Interaction 会通过 MQ 通知 Comment 把这条评论的 `like_amount` 加 1。

---

## 2. 一级评论和二级回复的区别

先搞清楚概念，不然看代码会晕。

**一级评论（First Comment）** 是直接挂在订单上的。一个订单只能有一条一级评论，因为用户点了这个订单，吃完了，给一个总体评价，这就够了。一级评论跟订单是一对一的关系（`order_id` 字段有唯一索引）。

**二级评论（Second Comment）** 是挂在一级评论下面的。一条一级评论可以有 N 条二级评论，展开来就是"评论区的讨论"。二级评论又分两种：

- **普通用户回复**：任何用户看到这条评论后都可以回复，就像微博的评论区。
- **商家回复**：被评论的商家可以回复这条评论，会打上 `is_merchant_comment = true` 的标记。前端通常会把这个标记的回复用特殊样式高亮显示，比如"商家回复"的标签。

**为什么要把评论分成两级？** 这是最常见的内容结构设计。一级评论是"根"，二级评论是"叶子"。如果你不区分层级，用户回复和商家回复全混在一起，评论区就变成了一团乱麻。分成两级后，前端可以轻松实现"展开查看回复"的效果，用户体验好很多。

**数据库设计的对应关系：**
- `first_comment` 表：存储一级评论，包含 `order_id`（唯一）、`user_id`、`merchant_id`、`meal_id`、`like_amount`（点赞数）、`second_comment_amount`（二级评论数）。
- `second_comment` 表：存储二级评论，包含 `first_comment_id`（父评论 ID）、`account_id`（回复者 ID）、`is_merchant_comment`（是否商家回复）、`like_amount`。

---

## 3. 点赞是怎么实现的（异步同步）

点赞功能分属两个模块：Interaction 管"谁点了谁的赞"，Comment 管"这条评论目前有多少赞"。它们是异步协作的，流程如下：

**第一步：用户点击点赞按钮。** 这个请求发到 Interaction 服务，Interaction 用 Redis BitMap 记录"用户 A 点赞了评论 X"这个状态，然后往 RabbitMQ 发一条消息，内容是 `{commentId: X, changeNumber: +1}`。

**第二步：Comment 模块消费点赞变更消息。** `ChangeFirstCommentLikeAmountConsumer` 监听着 `Interaction_Exchange_Change_First_Comment_Like_Amount_Queue` 这个队列，收到消息后，调用 `firstCommentMapper.updateLikeAmount` 把对应评论的 `like_amount` 字段加 1（或减 1，取决于 `changeNumber` 的正负）。

**为什么用异步而不是同步？** 因为点赞是一个高频操作，如果每次点赞都要同步等 Comment 服务更新完数据库才返回，用户体验会很差。用异步的话，Interaction 改完 Redis 就立刻返回"点赞成功"，Comment 后面慢慢更新数据库，用户完全感觉不到延迟。

**那如果 Comment 服务挂了怎么办？** 消息在 RabbitMQ 的队列里存着，等 Comment 服务恢复后会继续消费。点赞数可能会短暂不一致（Redis 里显示已点赞，但数据库里点赞数还没更新），但最终一定会一致。这就是"最终一致性"的设计思路。

**还有一个缓存层：** 一级评论的详细信息查询用到了 Caffeine 本地缓存（`FirstCommentCaffeine`）。当有人查询某条评论的详情时，先从 Caffeine 缓存里找，没找到再从 MySQL 查，查完写入缓存。缓存的过期时间由 Nacos 配置中心动态控制。这样一条热门评论被反复查看时，不会每次都打到数据库。

---

## 4. 为什么要调订单服务验证

看 `FirstCommentServiceImpl.insertComment` 这个方法，你会发现它在插入评论之前，先调了 Order 服务的一个接口：

```java
FirstCommentDTO firstComment = orderClient.commentSelect(orderId).getData();
```

**为什么要多此一举？** 因为不是随便哪个用户都能对任意订单发评论的。评论必须满足两个条件：

1. **这个订单必须存在**。你不能对不存在的订单写评论。
2. **这个订单必须是你的**。你不能帮别人写评论。

Comment 模块自己不知道订单属于谁，也不知道订单状态是否允许评论（比如订单还没完成就不该允许评论）。这些信息都在 Order 模块里。

**那为什么不直接让前端传用户 ID 和商家 ID 呢？** 因为前端传的参数不可信。任何人都可以修改请求参数，假装自己是另一个用户。通过 Feign 调用 Order 服务，Order 服务会从数据库里查出这个订单的真实信息（用户 ID、商家 ID、餐品信息等），然后返回给 Comment。Comment 拿着这些信息去写评论，数据来源是可靠的。

**这是一种"服务间信任"的设计**：Comment 信任 Order 返回的数据，因为它走的是内网 Feign 调用，不是外网 HTTP 请求。而且 Order 服务本身也有 JWT 鉴权，不是谁都能调的。

---

## 5. 亮点和坑

**亮点：**

- **Caffeine + Redis 双层缓存**。Caffeine 是 JVM 本地缓存，速度极快，但容量有限；Redis 是分布式缓存，容量大但要经过网络。这里用 Caffeine 缓存热点评论的详情，Redis 管理缓存失效通知（通过 `deleteAllCaffeine` 方法清除可能存在的旧缓存），各取所长。

- **二级评论的商家回复校验**。商家回复时，不仅要检查一级评论是否存在，还要检查这个商家是不是被评论的商家。如果不做这个校验，商家 A 就可以去商家 B 的评论下面假装回复，造成混乱。

- **MQ 解耦点赞数同步**。点赞数的变更不阻塞用户的点赞操作，由 MQ 异步完成。同时 `ChangeAmountDTO` 里的 `changeNumber` 可以是正数也可以是负数，点赞和取消点赞用同一个消费者，代码简洁。

- **图片删除的容错处理**。插入评论时如果数据库操作失败，会先删除刚才保存的图片文件，防止磁盘空间被垃圾文件占用。

**坑：**

- **一级评论和订单是一对一关系**。`order_id` 有唯一索引，如果同一个订单被重复提交评论请求，第二次会触发唯一键冲突。代码里用 `try-catch` 捕获了异常，但如果高并发场景下两个请求同时到达，可能一个成功一个报错，前端需要处理好"评论已存在"的提示。

- **Caffeine 缓存的过期时间需要谨慎设置**。如果过期时间太短，缓存命中率低，数据库压力大；如果太长，评论的点赞数更新后，缓存里还是旧数据。当前方案是在写入评论时主动清除缓存（`deleteAllCaffeine`），点赞数变更时依赖缓存自然过期，这意味着点赞数可能有短暂的不一致。如果业务要求强一致性，需要在点赞数变更的消费者里也主动清除缓存。

- **Feign 调用是同步的**。如果 Order 服务响应慢或挂了，Comment 的插入评论接口也会跟着超时或报错。可以考虑加 Hystrix/Sentinel 熔断降级，或者把订单验证也改成异步，但那样会引入更多复杂度，需要权衡。

---

## 6. 快速启动

**前置条件：**
- MySQL 8.0+（执行 `src/main/resources/MysqlCreate.sql` 建库建表）
- Redis 7.0+
- RabbitMQ 3.9+
- Nacos 2.0+（需要配置好 namespace、group 和相关的配置文件）
- Order 服务必须已启动（因为 Comment 启动后需要 Feign 调用 Order 的接口）

**启动步骤：**

1. 确保 Config、Util、DTO 三个公共模块已经编译安装到本地 Maven 仓库。
2. 确保 Nacos 上已经配置好了 `comment-self-dev.yaml`、`mq-name-bind-dev.yaml`、`common-param-rules-dev.yaml` 等配置文件。
3. 启动 Comment 服务的主类 `CommentApplication.java`。
4. 启动后，服务会注册到 Nacos，端口为 10011。

**验证：**
- 通过 Order 服务获取一个有效订单，调用 `POST /comment/first` 接口插入一条一级评论，检查 `first_comment` 表是否有记录。
- 调用 `GET /comment/first/detail?commentId=xxx` 查询评论详情，第二次查询时观察日志，确认是否命中了 Caffeine 缓存。
- 调用 `POST /comment/second` 插入一条二级回复，检查 `second_comment` 表是否有记录，同时检查对应一级评论的 `second_comment_amount` 是否自增了。
- 模拟 Interaction 服务发一条点赞变更消息到 MQ，检查 Comment 的消费者是否正常消费并更新了 `like_amount`。