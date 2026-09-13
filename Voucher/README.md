# Voucher（优惠券服务）

> 端口 **10006** | 服务名 **voucher** | 数据库 **seek_food_voucher**

---

## 1. 这个模块是干什么的？

Voucher 服务负责整个 SeekFood 平台里**优惠券**相关的所有操作。商家可以在这里创建优惠券（比如"满 30 减 5"），用户可以在活动里领取优惠券，下单时锁定优惠券、支付成功后正式使用，或者取消订单时回滚优惠券。

这个服务有两张核心表：一张是 `merchant_voucher`（商家发的券模板），另一张是 `voucher_connection`（用户和券的持有关系）。你可以把前者理解为"券的定义"，后者理解为"谁手里有这张券"。

---

## 2. 一张券的生命周期

一张优惠券从诞生到消亡，会经历下面这几个阶段，每个阶段都有对应的操作：

### 第一阶段：发券

商家在后台填写券的名称、描述、折扣金额、最低消费门槛、有效期等，然后点击发布。服务端会先校验参数（比如折扣金额不能比最低消费还大），然后检查冷却期，最后用 Redis 计数器生成一个券 ID，写入 `merchant_voucher` 表。

### 第二阶段：领券

用户参加某个营销活动，活动服务（Promotion）会通过消息队列发一条消息到 Voucher 服务。`RegisterVoucherConnectionConsumer` 收到消息后，先查一下这张券的详细信息（走缓存），拿到有效期，然后在 `voucher_connection` 表里插入一条记录，记录"用户 ID + 券 ID + 活动 ID + 有效期"，状态为未锁定、未使用。

### 第三阶段：锁定

用户下单时选择了优惠券，订单服务会通过 Feign 调用 Voucher 服务的 `lock` 接口。这个操作会把券的 `is_lock` 设为 true，同时记录下关联的订单 ID。为什么叫"锁定"而不是"使用"？因为用户可能最终不付款，券还得还回去。这个接口上标注了 `@GlobalTransactional`（Seata 全局事务），如果订单创建失败，锁定操作会自动回滚。

### 第四阶段：使用

用户支付成功后，订单服务发 MQ 消息到 `Fund_Exchange_Use_Voucher_Queue`。`UseVoucherConsumer` 消费到消息后，把券的 `is_use` 设为 true，表示这张券已经被正式消费了。然后它再发一条确认消息给订单服务，闭环整个流程。

### 第五阶段：回滚

如果订单取消或者支付超时，订单服务会发 MQ 消息到 `Order_Exchange_Rollback_Voucher_Queue`。`RollbackVoucherConsumer` 收到后，把券的 `is_lock` 设回 false，`order_id` 清空，同时清除缓存。这样券就回到了"已领取但未使用"的状态，用户可以下次再用。

---

## 3. 为什么用 MQ 处理券操作？

你可能会问：为什么锁定、使用、回滚这些操作不直接调接口，而要绕一圈发 MQ 消息呢？

**解耦是第一原因。** 订单服务不需要知道 Voucher 服务的内部逻辑，它只需要发消息说"我下单了，帮我锁定券"或者"订单取消了，帮我把券还回去"。如果以后 Voucher 服务的逻辑变了（比如使用券时还要发积分），订单服务完全不用改。

**削峰是第二原因。** 秒杀场景下，大量用户同时下单，如果同步调用 Voucher 服务，会把 Voucher 也拖垮。用 MQ 的话，消息在队列里排队，Voucher 服务按自己的节奏消费，不会被瞬间流量冲垮。

**可靠性是第三原因。** 消息队列保证了"至少一次"投递，即使 Voucher 服务宕机了，消息也不会丢，重启后继续消费。

不过这也带来了一个问题：**消息可能重复消费**。所以券的锁定使用都用了乐观锁（`where is_lock = false`），保证重复操作不会出错。

---

## 4. 亮点和坑

**亮点：**

- **完整的生命周期管理**：从发券到领券到锁定到使用到回滚，每一步都有清晰的状态流转，不是简单的"增删改查"。
- **Seata 全局事务**：`lock` 接口上标注了 `@GlobalTransactional`，确保券锁定和订单创建要么都成功，要么都失败，不会出现"券锁了但订单没创建"的尴尬情况。
- **Caffeine 双缓存**：券模板和券持有关系各有一套 Caffeine 缓存，互不干扰。查询都走 `getAndAutoLoad` 三级缓存，只有写操作才穿透到数据库。
- **MQ 异步解耦**：使用券、回滚券、注册券关联全部通过 MQ 异步处理，订单服务、活动服务、Voucher 服务之间没有直接调用链，各自独立部署。
- **分布式锁控制并发**：券的锁定操作通过 SQL 的 `where` 条件做乐观锁，防止同一张券被两个订单同时锁定。

**坑：**

- 消息乱序可能是个问题。比如"使用券"的消息比"锁定券"的消息先到，这时候券还没锁定，使用操作就会失败。解决方案是依赖消息队列的顺序性，或者在使用时做状态校验。
- Seata 全局事务会增加延迟，锁定时需要等待事务协调器确认。如果 Seata 服务挂了，所有锁定操作都会失败。
- 优惠券的缓存是个双刃剑：缓存了券信息后，如果商家修改了券的规则，缓存可能还没过期，导致用户看到旧数据。这个服务目前是用"查询时自动缓存，不主动刷新"的策略，所以更新后需要等缓存自然过期。

---

## 5. 快速启动

### 环境要求

- JDK 21+
- Maven 3.9+
- MySQL（需要先执行 `src/main/resources/MysqlCreate.sql` 建库建表）
- Redis（缓存、冷却期、ID 计数器）
- RabbitMQ（使用券、回滚券、注册券关联）
- Nacos（服务注册发现 + 配置管理）
- Seata（分布式事务，用于锁定券操作）

### 启动步骤

1. 建库：执行 `MysqlCreate.sql`。
2. 确认 Nacos 中已配置好 `COMMON_GROUP`、`MQ_GROUP`、`SEEK_FOOD_GROUP` 的相关配置文件。
3. 启动 Seata Server（如果不用分布式事务可以跳过，但锁券功能会受影响）。
4. 启动 Redis 和 RabbitMQ。
5. 运行 `VoucherApplication.java` 主类，端口 10006。

### 核心接口一览

| 接口 | 方法 | 说明 |
|------|------|------|
| `/merchantVoucher/insert` | POST | 商家发布新优惠券 |
| `/merchantVoucher/getSimple` | GET | 商家查看自己的券列表 |
| `/merchantVoucher/getSimpleEffective` | GET | 查看有效期内券列表 |
| `/merchantVoucher/getDetail` | GET | 查看券详情（走缓存） |
| `/merchantVoucher/exist` | GET | Feign 接口：确认券是否属于该商家 |
| `/voucherConnection/getSimple` | GET | 用户查看自己的券列表 |
| `/voucherConnection/getSimpleEffective` | GET | 用户查看有效券列表 |
| `/voucherConnection/getDetail` | GET | 用户查看券持有详情 |
| `/voucherConnection/lock` | GET | Feign 接口：锁定券（Seata 全局事务） |
| `/voucherConnection/check` | GET | Feign 接口：检查券是否满足条件 |

### MQ 消费者一览

| 消费者 | 监听的队列 | 触发场景 |
|--------|-----------|---------|
| `RegisterVoucherConnectionConsumer` | 活动注册券队列 | 用户通过活动领券 |
| `UseVoucherConsumer` | 资金使用券队列 | 用户支付成功，正式使用券 |
| `RollbackVoucherConsumer` | 订单回滚券队列 | 订单取消，退回券 |

### 依赖关系

本模块依赖 **Config**（Nacos 配置）、**Util**（工具类）、**DTO**（数据传输对象）三个公共模块，不直接依赖其他业务模块。