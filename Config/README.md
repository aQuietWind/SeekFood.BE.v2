# Config（配置中心）

> 非服务模块（没有端口），所有业务模块的公共依赖

## 1. 这个模块是干什么的？

Config 模块是整个 SeekFood 项目的**"配置中台"**。它不处理任何业务逻辑，不监听任何端口，但它被所有 16 个业务模块共同依赖。

你可以把它想象成一个"插座"：每个业务模块就像一盏灯，它们需要电（配置）才能工作。Config 模块就是这个插座，它从 Nacos 配置中心取电，然后统一分发给所有灯。没有 Config 模块，每个业务模块都得自己"拉电线"（写配置代码），又乱又容易出错。

具体来说，Config 模块统一管理了这些东西：
- **JWT 密钥配置**：不同角色（用户、商家、骑手、管理员）用不同的密钥签发 Token
- **Redis Key 规范**：所有 Redis Key 的前缀、过期时间都在这里统一定义
- **参数校验规则**：手机号正则、密码长度、ID 号段……所有校验规则集中管理
- **MQ 交换机配置**：14 个业务交换机的名称、队列、路由键都在这里声明
- **请求拦截器**：Token 拦截、Feign 透传、全局异常处理
- **Sentinel 降级处理**：限流、熔断后的自定义返回

## 2. 为什么要把配置集中管理？

假设你是一个刚入职的工程师，老板让你"把 JWT 的过期时间从 7 天改成 14 天"。如果配置散落在 16 个服务的代码里，你需要改 16 个地方，然后每个服务重新编译、重新部署。这还没算上可能改漏的风险。

有了 Config 模块，你只需要在 Nacos 控制台里改一个 YAML 文件，保存，然后所有服务自动生效——**一台机器都不用重启**。

这就是"配置集中管理"的核心价值：**一处修改，处处生效**。而且它还带来了另一个好处：**类型安全**。传统做法是用 `@Value("${xxx}")` 注入一个字符串，编译器不会帮你检查这个配置存不存在。而 Config 模块用 `@ConfigurationProperties` 把 YAML 映射成 Java Bean，配错了编译不过，IDE 还有自动补全。

## 3. @Import 注解是怎么实现模块化装配的？

这是 Config 模块最精妙的设计。你可能会想：Config 模块有这么多配置类，如果每个业务模块启动时全都加载，会不会很浪费？而且有些配置（比如 User 服务的专属配置）Merchant 服务根本不需要。

解决方法是"按需装配"。Config 模块提供了一组自定义注解，比如 `@CommonImport`、`@MQImport`、`@UserImport`、`@MerchantImport` 等等。每个业务模块在启动类上只需要加自己需要的注解：

```java
@SpringBootApplication
@CommonImport   // 引入 JWT、Redis Key、参数规则（所有服务都需要的公共配置）
@MQImport       // 引入 MQ 交换机配置（需要发消息的服务才加）
public class UserApplication { ... }
```

这个注解背后是怎么工作的？`@CommonImport` 内部其实是一个 `@Import(CommonSubConfig.class)`，`CommonSubConfig` 是一个 `@Configuration` 类，它用 `@EnableConfigurationProperties` 绑定对应的 Nacos 配置类，然后通过 `@Bean` 暴露出来。

**还有一个很巧妙的设计：`@ConditionalOnWebApplication`。** 有些配置（比如 Token 拦截器）只在 Servlet 类型的 Web 服务里有用，如果在 WebFlux 网关里加载会冲突。Config 模块在所有基础设施组件上都加了 `@ConditionalOnWebApplication(type = SERVLET)`，这样网关自动跳过这些组件，不会冲突。

**另外，`@ConditionalOnMissingBean` 允许业务模块覆盖默认实现。** 比如大部分服务用默认的缓存配置，但某个服务想自定义，只需要自己声明一个同类型的 Bean，Config 模块的默认 Bean 就不会创建。

## 4. 拦截器和异常处理

Config 模块提供了两个关键的拦截器，它们不需要任何注解，只要引入 Config 依赖就自动生效（通过 `AutoConfiguration.imports` 文件声明）。

**TokenInterceptor（Token 拦截器）：** 每个请求到达业务服务后，这个拦截器会从请求头里取出 `Token-Id`，写入 `TokenIdContext`（一个 ThreadLocal）。这样业务代码里任何地方都能拿到"当前是谁在操作"，不需要每个方法都传一个 userId 参数。请求结束后，拦截器清理 ThreadLocal，防止内存泄漏。

**FeignTokenInterceptor（Feign 透传拦截器）：** 当业务服务通过 Feign 调用其他服务时，这个拦截器会自动把当前请求的 Token-Id 塞到 Feign 请求头里。这样被调用的服务也能知道"是谁在操作"，不需要重新验证 Token。这就是"信任链"的设计：网关验证一次，后面的服务之间互相信任。

**GlobalRequestExceptionHandler（全局异常处理）：** 这是一个 `@RestControllerAdvice`，统一处理所有异常。它分层处理：先处理业务异常（`BizException`），再处理参数校验异常，最后兜底处理 `RuntimeException`。所有异常都返回统一的 `Result` 格式，前端只要看 `code` 字段就能区分错误类型。

## 5. 亮点和坑

**亮点：**

- **@RefreshScope 动态刷新**：所有 Nacos 配置类都用 `@RefreshScope`，改配置不重启。但要注意，`@RefreshScope` 会产生代理对象，和 `@Transactional` 一起用可能有问题。建议把配置读取和业务逻辑分开。
- **统一异常处理**：全项目用同一套 `Result<T>` + `ErrorCodeEnum`，前端按 code 分支处理，不需要每个服务自己写错误响应。
- **Sentinel 深度集成**：自定义了限流降级返回、全局 URL 聚合、请求来源识别，覆盖了服务治理的核心场景。
- **MQ 高可靠**：配置了消息确认回调（ReturnsCallback）和死信队列（RepublishMessageRecoverer），消费失败的消息自动转入死信，不会丢。

**需要注意的坑：**

- **Nacos 日志太多**：在 `logback.xml` 里单独调高了 Nacos 相关包的日志级别，否则日志文件会被刷爆。
- **启动速度受 Nacos 网络影响**：配置类统一用了 `@Lazy` 懒加载，不在启动时全部初始化，改善启动速度。
- **Seata 异常包装**：Seata 分布式事务会把业务异常包装成 `RuntimeException`，全局异常处理器里需要特殊解包才能拿到原始错误码。

## 6. 快速开始

Config 模块不需要单独启动，它被其他业务模块依赖。

**在你自己的业务模块中使用：**

1. 在 `pom.xml` 里添加依赖：
```xml
<dependency>
    <groupId>com.seek.food</groupId>
    <artifactId>Config</artifactId>
</dependency>
```

2. 在启动类上添加需要的注解：
```java
@SpringBootApplication
@CommonImport   // 必须：JWT、Redis Key、参数规则
@MQImport       // 可选：需要 MQ 时才加
@UserImport     // 可选：User 服务专属配置
public class MyApplication { ... }
```

3. 在代码里直接注入使用：
```java
@Service
public class MyService {
    private final JWTConfig jwtConfig;
    private final CommonParamRulesConfig rulesConfig;

    public void doSomething(String phone) {
        rulesConfig.phoneNumberCheck(phone); // 校验手机号格式
        // ...
    }
}
```

**TokenInterceptor、GlobalRequestExceptionHandler 等基础设施不需要任何配置，引入依赖即自动生效。**