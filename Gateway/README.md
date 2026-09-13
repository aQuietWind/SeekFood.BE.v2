# 🚪 Gateway 网关 — 整个系统的"门卫"

> 端口：**10000** | 服务名：`gateway`

---

## 这个模块是干什么的？

你可以把网关想象成一个大楼的**前台保安**。所有进入大楼的人（请求）都必须经过他。他要做三件事：

1. **验明正身**：你是谁？你的 Token 有效吗？
2. **指路**：你要去 3 楼（用户服务）还是 5 楼（订单服务）？
3. **拦人**：你被拉黑了？1 分钟请求了 100 次？出去！

**为什么需要一个网关？** 如果没有网关，每个业务服务都要自己验证 Token、自己处理限流，代码重复不说，万一哪个服务忘了验证，就等于给系统开了个后门。把安全逻辑统一放在网关，业务服务就可以专心处理业务。

---

## 一个请求进来后，经历了什么？

```
用户请求到达网关（端口 10000）
        ↓
    ┌───────────────────────┐
    │  第一道门：TokenFilter  │  ← 执行顺序第 1
    │  "你是谁？"             │
    └───────────────────────┘
        ↓
    检查这个请求路径是否需要登录？
        ├── 不需要（如登录、注册接口）→ 直接放行
        ├── 被封禁的路径？→ 直接拒绝
        └── 需要登录 → 验证 Token
                ↓
            从 Cookie 中取出 Token
                ↓
            Token 长什么样？
            格式：前缀 + 分隔符 + 真实Token
            比如：user_abc123def456
                  ↑ 前缀表示角色类型
                ↓
            根据前缀找到对应的密钥，解密验证
                ↓
            验证通过 → 再去 Redis 检查这个 Token 是否还在有效期内
                ↓
            全部通过 → 把用户 ID 塞到请求头里，放行
        ↓
    ┌───────────────────────┐
    │  第二道门：RequestFilter│  ← 执行顺序第 2
    │  "你被拉黑了吗？"       │
    └───────────────────────┘
        ↓
    检查 IP 是否在黑名单里？
        ├── 先查本地缓存（Caffeine，微秒级）
        ├── 没有再查 Redis（毫秒级）
        └── 在黑名单 → 拒绝
        ↓
    检查这个 IP 最近请求了多少次？
        ├── 用 Redis 计数器记录
        ├── 超过阈值 → 自动加入黑名单，封禁 N 小时
        └── 没超过 → 放行
        ↓
    请求被转发到目标业务服务
```

---

## 设计细节：为什么这样做？

### 1. Token 为什么放在 Cookie 里，而不是请求头里？

**安全**。如果 Token 放在请求头里，前端 JavaScript 可以读取它。如果网站被 XSS 攻击（攻击者注入恶意脚本），Token 就会被偷走。

放在 **HttpOnly Cookie** 里，浏览器会禁止 JavaScript 读取这个 Cookie，只有后端能拿到。这样即使网站被 XSS 攻击，Token 也不会泄露。

### 2. Token 为什么用"前缀+分隔符"的格式？

这个系统有四种角色：普通用户、商家、骑手、管理员。每种角色用不同的密钥签发 Token。

网关收到 Token 后，需要知道用哪把密钥来解密。做法是：Token 的前面加上角色前缀，比如 `user_xxx` 表示用户 Token、`merchant_xxx` 表示商家 Token。网关根据前缀找到对应的密钥，解密验证。

**这样做的好处**：不需要额外查数据库判断角色，直接从 Token 本身就能区分。而且不同角色的密钥互相隔离，即使某个角色的密钥泄露，不影响其他角色。

### 3. 为什么验证 Token 后还要查 Redis？

Token 签发后，在有效期内（比如 7 天）都是合法的。但如果用户主动退出登录，或者管理员强制踢人，Token 还在有效期内，怎么让它失效？

这个项目的做法是：**在 Redis 里维护一个"在线 Token 池"**（用 ZSet 实现）。用户登录时把 Token 加入池子，退出时从池子移除。网关验证 Token 时，除了检查 Token 本身是否合法，还要检查它是否在 Redis 池子里——不在池子里说明已失效。

### 4. 黑名单为什么用 Caffeine + Redis 双层？

**纯 Redis**：每次请求都要查一次 Redis，多一次网络开销（~1ms）。
**纯 Caffeine**：本地内存快，但多实例部署时数据不同步。

**双层方案**：先查 Caffeine（本地内存，微秒级），如果 Caffeine 里没有再查 Redis，然后把结果缓存在 Caffeine 里。当 Redis 里的黑名单更新时，通过消息通知所有实例清除 Caffeine 缓存。

---

## 这个模块依赖了什么？

网关本身不操作数据库，但依赖了：

- **Nacos**：注册中心（发现有哪些服务可以转发）+ 配置中心（放行路径、封禁路径、JWT 密钥等配置都从这里读）
- **Redis**：存 Token 池、存黑名单、存请求计数器
- **Config 模块**：读取 JWT 配置、Redis Key 配置、请求路径配置
- **Util 模块**：JWT 工具类（生成和验证 Token）

---

## 配置是怎么管理的？

网关的配置全部放在 Nacos 里，不是写在本地配置文件里。这样做的好处是：**改配置不需要重启服务**。

比如，突然发现某个接口被攻击，想在网关层面封禁它——只需要在 Nacos 里把路径加到封禁列表，网关会自动读取新配置，立即生效。

```yaml
# Nacos 里的配置长这样（简化版）：
# 放行路径（不需要登录就能访问）
allowPaths:
  - /user/login
  - /user/register
  - /merchant/login

# 封禁路径（直接拒绝）
rejectPaths:
  - /admin/secret-api

# JWT 配置
jwt:
  requestTokenName: access_token   # Cookie 里取 Token 的 key
  headerSeparator: "_"              # Token 前缀分隔符
  headerTokenName: ownId            # 验证通过后，把用户 ID 放到这个请求头
  allJWTData:
    - headerSign: user              # 用户 Token 前缀
      secretKey: user-secret-key
    - headerSign: merchant          # 商家 Token 前缀
      secretKey: merchant-secret-key
    - headerSign: rider             # 骑手 Token 前缀
      secretKey: rider-secret-key
```

---

## 网关的"坑"和注意事项

### 1. Spring Cloud Gateway 是响应式的

网关用的是 Spring WebFlux（响应式编程），不是传统的 Spring MVC。这意味着你不能用传统的 `HttpServletRequest`/`HttpServletResponse`，而是用 `ServerWebExchange`。如果你以前只写过 Spring MVC，这里需要适应一下。

### 2. 网关不处理业务逻辑

新手常犯的错误：在网关里写业务逻辑。网关只做**路由和鉴权**，不应该处理任何业务。如果发现自己在网关里写了复杂的判断逻辑，停下来想想——这段逻辑是不是应该放在业务服务里？

---

## 如何本地启动？

```bash
# 1. 确保 Nacos 和 Redis 已经启动
# 2. 在 Nacos 中导入配置（Gateway 需要 COMMON_GROUP 和 SEEK_FOOD_GROUP 的配置）
# 3. 启动网关
cd Gateway
mvn spring-boot:run

# 4. 验证
curl http://localhost:10000/actuator/health
```

---

## 学习要点

如果你在学网关，这几个知识点值得深入：

1. **Spring Cloud Gateway 的路由规则**：路径匹配、负载均衡、lb:// 协议
2. **GlobalFilter 机制**：过滤器的执行顺序、`@Order` 注解
3. **HttpOnly Cookie 的安全原理**：为什么能防 XSS
4. **JWT 多密钥管理**：不同角色不同密钥的设计思路
5. **响应式编程**：WebFlux 和传统 Spring MVC 的区别

---

[← 返回项目首页](../README.md)