# DTO（数据对象）

> 非服务模块（没有端口），定义所有服务间通信的数据结构

## 1. 这个模块是干什么的？

DTO 的全称是 Data Transfer Object，翻译过来就是"数据传输对象"。它的作用很简单：**定义数据长什么样**。

在微服务架构里，服务之间需要频繁地传递数据。比如 Order 服务调用 User 服务查用户信息，Order 服务需要知道"用户信息包含哪些字段、每个字段是什么类型"。如果每个服务都自己定义一套，就会出现"Order 服务以为用户名是 `userName`，User 服务返回的却是 `name`"这种对不上的情况。

DTO 模块就是**所有服务之间的"数据约定"**。它把每一类数据（用户、订单、菜品、优惠券……）的结构定义好，放在一个独立的模块里，所有服务都引用这个模块。这样服务 A 传给服务 B 的数据，字段名、类型、格式都是统一且确定的。

## 2. Result<T> 的设计（统一响应格式）

`Result<T>` 是整个项目里最重要的 DTO，没有之一。它定义了**所有接口的返回格式**。

```java
public class Result<T> {
    private Integer code;    // 业务状态码，比如 200 表示成功，14101 表示参数错误
    private String msg;      // 提示信息，比如 "操作成功" 或 "手机号格式错误"
    private T data;          // 实际数据，泛型，可以是 UserDTO、OrderDTO 或 null
}
```

**为什么需要统一格式？** 假设没有 `Result`，每个 Controller 返回值格式都不一样——有人返回 `{"success": true, "user": ...}`，有人返回 `{"status": "ok", "data": ...}`。前端同学会疯掉的，因为每个接口都要写不同的解析逻辑。

有了 `Result<T>`，前端只需要做一件事：**检查 `code` 字段**。`code == 200` 就是成功，`code` 在 14xxx 范围就是业务异常，`code` 在 15xxx 范围就是服务异常。然后根据 `code` 决定是正常展示数据、弹错误提示、还是跳转登录页。

`Result<T>` 提供了几个静态工厂方法让你快速创建响应：
- `Result.success(data)` —— 成功，带数据
- `Result.success()` —— 成功，没数据（比如删除操作）
- `Result.error(errorCodeEnum)` —— 失败，用错误码枚举

**为什么泛型是 T？** 因为 data 字段可以是任何类型——查询用户返回 `Result<UserDTO>`，查询订单列表返回 `Result<List<OrderDTO>>`，删除操作返回 `Result<Void>`。泛型保证了类型安全，IDE 能自动推断出 `getData()` 的返回类型。

## 3. 各业务域的 DTO 是怎么组织的

DTO 模块按**业务域**分包，每个包对应该域一个服务：

```
com.seek.food.dto/
├── Common/          # 通用 DTO（Result、分页、地理位置等）
├── User/            # 用户相关（UserDTO）
├── Merchant/        # 商家相关（MerchantDTO、MerchantEsDTO）
├── Meal/            # 菜品相关（MealDTO）
├── Order/           # 订单相关（OrderDTO、RiderOrderEsDTO）
├── Fund/            # 资金相关（FundDTO、各种流水 DTO）
├── Voucher/         # 优惠券相关（MerchantVoucherDTO、VoucherConnectionDTO）
├── Promotion/       # 营销活动相关（MerchantLoginPromotionDTO、MerchantGrabPromotionDTO）
├── Employee/        # 员工相关（EmployeeDTO）
├── Rider/           # 骑手相关（RiderDTO）
├── Comment/         # 评论相关（FirstCommentDTO、SecondCommentDTO）
├── Chat/            # 聊天相关（ChatRoomDTO、ChatRecordDTO）
└── Admin/           # 管理后台相关（SuggestionDTO）
```

这个组织方式的好处是**一眼就能看出数据属于哪个服务**。当你需要修改"订单 DTO 加一个字段"时，直接去 `Order/` 包里找，不会影响其他服务。

**DTO 和数据库表是一一对应的吗？** 不完全是。有些 DTO 直接映射数据库表（比如 `EmployeeDTO` 的字段和 `employee` 表基本一致），有些 DTO 是多个表的聚合（比如 `OrderDTO` 包含了订单基础信息、商品快照、地址信息，这些在数据库里是分开的表），还有一些 DTO 是专门给 Elasticsearch 用的（比如 `MerchantEsDTO`，用 `@Document` 和 `@Field` 注解标记 ES 索引映射）。

**MQ 消息也有专用 DTO。** 比如 `VoucherConnectionMQDTO` 只有三个字段：`voucherId`、`userId`、`promotionId`。因为 MQ 消息传的是"发生了什么事件"，而不是完整的业务数据，所以 MQ 的 DTO 通常只包含必要的标识字段，体积小、序列化快。

## 4. 亮点和坑

**亮点：**

- **统一响应格式**：`Result<T>` 让前端只需要一种解析逻辑，团队协作效率高。
- **按业务域分包**：包名和模块名对应，新人能快速找到需要的 DTO。
- **ES 专用 DTO**：`EsSearchResult<T>` 封装了 ES 的分页搜索结果（结果列表 + 分页游标），用起来比直接操作 ES 原生对象方便得多。
- **Lombok 简化代码**：所有 DTO 都用 `@Data`、`@NoArgsConstructor`、`@AllArgsConstructor` 注解，不用手写 getter/setter。

**需要注意的坑：**

- **DTO 不要包含业务逻辑**：DTO 是纯数据载体，不应该有任何数据库操作或远程调用。如果发现 DTO 里需要写逻辑，那应该放到 Service 层。
- **修改 DTO 要评估影响**：DTO 被所有服务引用，改一个字段可能影响十几个服务。尤其是删字段或改类型，一定要先确认所有使用方。
- **避免循环依赖**：DTO 模块不能依赖任何业务模块，否则会形成循环依赖。它只依赖 Util 模块（因为要用 `ErrorCodeEnum` 等基础类型）。
- **序列化兼容性**：如果 DTO 要存 Redis 或发 MQ，确保字段类型能被 Jackson 序列化。常见坑是 `LocalDateTime`，需要额外的 `JavaTimeModule`。

## 5. 快速开始

**引入依赖：**
```xml
<dependency>
    <groupId>com.seek.food</groupId>
    <artifactId>DTO</artifactId>
</dependency>
```

**在 Controller 中使用：**
```java
import com.seek.food.dto.Common.Result;
import com.seek.food.dto.User.UserDTO;

@RestController
public class UserController {

    @GetMapping("/user/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.findById(id);
        return Result.success(user);
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        return Result.error(e.getErrorCode());
    }
}
```

**创建新的 DTO 时：**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewFeatureDTO {
    private Long id;
    private String name;
    private LocalDateTime createTime;
}
```

放在 `com.seek.food.dto` 下对应业务域的包里即可。