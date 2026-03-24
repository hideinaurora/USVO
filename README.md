# 报名管理系统 API 文档

## 项目简介

本项目是一个基于 Spring Boot 的活动报名管理系统，提供管理端和移动端两套 API 接口。系统支持活动发布、用户报名、在线支付、退款管理等功能，并使用分布式锁保证高并发场景下的名额管理。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis Plus | 3.5.1 | ORM 框架 |
| MySQL | 8.0.33 | 数据库 |
| Redis | - | 缓存、分布式锁 |
| Redisson | 3.15.6 | Redis 客户端 |
| JWT | 3.8.1 | 令牌认证 |
| RabbitMQ | - | 消息队列 |
| Swagger (OpenAPI 3.0) | 1.7.0 | API 文档 |
| Kaptcha | 0.0.9 | 验证码生成 |

## 项目结构

```
src/main/java/org/example/
├── aop/                          # AOP 切面
│   ├── annotation/               # 自定义注解
│   │   ├── ApiAuth.java         # 角色权限枚举
│   │   ├── ApiIdempotent.java   # 幂等性注解
│   │   └── RequiresPermissions.java # 权限校验注解
│   ├── aspect/                   # 切面实现
│   │   └── IdempotentAspect.java # 幂等性切面
│   └── filter/                   # 过滤器
│       ├── PermissionAspect.java # 权限校验切面
│       ├── RequestFilter.java    # 请求过滤器（token、traceId）
│       └── WebLogAspect.java     # 日志切面
├── common/                       # 公共类
│   ├── ApiResponse.java         # 统一响应结果
│   └── PageResult.java          # 分页结果
├── config/                       # 配置类
│   ├── exception/               # 异常处理
│   │   ├── CommonJsonException.java    # 业务异常
│   │   ├── GlobalExceptionHandler.java # 全局异常处理器
│   │   ├── UnauthenticatedException.java # 未认证异常
│   │   └── UnauthorizedException.java   # 未授权异常
│   ├── MybatisPlusConfig.java   # MyBatis Plus 配置
│   ├── RedisConfig.java         # Redis 配置
│   └── SwaggerConfig.java       # Swagger 配置
├── controller/                   # 控制器层
│   ├── AdminController.java     # 管理端用户接口
│   ├── AppController.java       # 移动端用户接口
│   ├── ApplyController.java     # 活动管理接口
│   ├── PaymentCallbackController.java # 支付回调接口
│   ├── RefundController.java    # 退款管理接口
│   └── WechatController.java    # 微信接口
├── dto/                          # 数据传输对象
├── entity/                       # 实体类
│   ├── activity/                # 活动相关实体
│   ├── basic/                   # 基础实体
│   ├── failed/                  # 失败记录实体
│   └── sys/                     # 系统实体
├── mapper/                       # 数据访问层
├── service/                      # 业务逻辑层
├── mq/                          # 消息队列
├── task/                        # 定时任务
├── utils/                       # 工具类
└── vo/                          # 视图对象
```

## Swagger API 文档

### 访问地址

启动项目后，访问以下地址查看 Swagger API 文档：

```
http://localhost:9046/swagger-ui.html
```

或使用 OpenAPI 格式：

```
http://localhost:9046/v3/api-docs
```

### 配置说明

Swagger 配置位于 `org.example.config.SwaggerConfig`，当前配置信息：

- **标题**: 报名管理系统API
- **描述**: 管理端用户登录接口文档
- **版本**: 1.0.0
- **联系人**: ckd

### 接口分组

Swagger 文档按 Controller 的 `@Tag` 注解自动分组：

1. **管理端用户** - 管理员登录、验证码、令牌刷新
2. **移动端用户** - 用户注册、登录、活动查询、报名、退款申请
3. **活动管理** - 活动的增删改查、名额管理
4. **退款管理** - 退款审核、查询
5. **支付回调** - 支付结果通知
6. **微信** - 微信登录

## 接口说明

### 统一响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 200,        // 状态码：200表示成功
  "message": "success", // 响应消息
  "data": {}          // 响应数据
}
```

常见错误码：
- `200`: 成功
- `401`: 未登录或登录失效
- `403`: 权限不足
- `405`: 请求方式不符
- `500`: 服务器异常

### Token 认证

需要认证的接口需要在请求头中携带 JWT Token：

```
Authorization: eyJhbGciOiJIUzI1NiJ9...
```

### 管理端接口（`/api/admin`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/admin/login` | POST | 管理员登录 | 公开 |
| `/api/admin/captcha` | GET | 获取验证码 | 公开 |
| `/api/admin/refresh-token` | POST | 刷新令牌 | 公开 |

**登录示例**：
```json
POST /api/admin/login
{
  "username": "admin",
  "password": "123456",
  "captchaKey": "captcha_key",
  "captchaCode": "1234"
}
```

### 移动端接口（`/api/app`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/app/register` | POST | 用户注册 | 公开 |
| `/api/app/login` | POST | 用户登录 | 公开 |
| `/api/app/activities` | GET | 查询可报名活动列表 | USER |
| `/api/app/enrolled-activities` | GET | 查询已报名活动列表 | USER |
| `/api/app/apply` | POST | 用户报名活动 | USER |
| `/api/app/refund/apply` | POST | 申请退款 | USER |

**报名示例**：
```json
POST /api/app/apply
Authorization: <token>
{
  "applyId": 1
}
```

### 活动管理接口（`/api/apply`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/apply/page` | POST | 分页查询活动列表 | ADMIN |
| `/api/apply/list` | GET | 查询活动列表（不分页） | ADMIN |
| `/api/apply/{applyId}` | GET | 查询活动详情 | ADMIN,USER |
| `/api/apply/users/page` | POST | 分页查询报名学生列表 | ADMIN |
| `/api/apply/users/{applyId}` | GET | 查询报名学生列表 | ADMIN |
| `/api/apply/add` | POST | 新增活动 | ADMIN |
| `/api/apply/update` | PUT | 修改活动 | ADMIN |
| `/api/apply/{applyIds}` | DELETE | 删除活动 | ADMIN |
| `/api/apply/quota/increase` | PUT | 增加活动名额 | ADMIN |
| `/api/apply/quota/decrease` | PUT | 减少活动名额 | ADMIN |
| `/api/apply/quota/{applyId}` | GET | 获取剩余名额 | ADMIN,USER |

**分页查询示例**：
```json
POST /api/apply/page
Authorization: <token>
{
  "pageNum": 1,
  "pageSize": 10,
  "name": "活动名称"
}
```

### 退款管理接口（`/api/refund`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/refund/page` | POST | 分页查询退款列表 | ADMIN |
| `/api/refund/examine` | POST | 退款审核 | ADMIN |

## 权限校验实现

### 实现机制

项目使用 **AOP + 自定义注解** 的方式实现权限校验，核心组件：

1. **`@RequiresPermissions` 注解** - 标记需要权限校验的接口
2. **`ApiAuth` 枚举** - 定义角色类型（ADMIN、USER）
3. **`PermissionAspect` 切面** - 执行权限校验逻辑

### 角色定义

```java
public enum ApiAuth {
    ADMIN(1),  // 管理员
    USER(2);   // 普通用户
    private final Integer type;
}
```

### 使用方式

在 Controller 方法上添加 `@RequiresPermissions` 注解：

```java
@PostMapping("/page")
@RequiresPermissions(value = "apply:page", apiAuth = {ApiAuth.ADMIN})
public ApiResponse<PageResult<ApplyDetailVO>> queryPage(@RequestBody ApplyQueryDTO queryDTO) {
    // 业务逻辑
}
```

参数说明：
- `value`: 权限标识（用于日志记录）
- `apiAuth`: 允许访问的角色数组

### 校验流程

```
1. 请求到达 → RequestFilter 提取 Token 存入 MDC
2. PermissionAspect 拦截 @RequiresPermissions 注解的方法
3. 从 MDC 获取 Token
4. JWTUtil 解析 Token 获取用户角色
5. 比对用户角色与注解要求的角色
6. 校验通过 → 继续执行
   校验失败 → 抛出 UnauthenticatedException
```

### 核心代码

**权限切面** (`PermissionAspect.java:34-69`):
```java
@Before("@annotation(org.example.aop.annotation.RequiresPermissions)")
public void before(JoinPoint joinPoint) {
    String token = MDC.get("token");
    if (token == null) {
        throw new UnauthenticatedException();
    }
    TokenDTO tokenDTO = JWTUtil.verifyToken(token);
    if (tokenDTO == null || !hasPermission(joinPoint, tokenDTO)) {
        throw new UnauthenticatedException();
    }
}

private Boolean hasPermission(JoinPoint joinPoint, TokenDTO tokenDTO) {
    RequiresPermissions a = methodSignature.getMethod().getAnnotation(RequiresPermissions.class);
    ApiAuth[] auth = a.apiAuth();
    for (ApiAuth logical : auth) {
        if (logical.getType().equals(tokenDTO.getRoleId())) {
            return true;
        }
    }
    return false;
}
```

## 全局异常捕获

### 实现机制

使用 `@ControllerAdvice` + `@ExceptionHandler` 实现全局异常统一处理。

### 核心组件

**`GlobalExceptionHandler`** 位于 `org.example.config.exception`，处理以下异常：

| 异常类型 | 处理方法 | 返回码 | 说明 |
|---------|---------|--------|------|
| `Exception` | `defaultErrorHandler` | 500 | 通用异常捕获 |
| `HttpRequestMethodNotSupportedException` | `httpRequestMethodHandler` | 405 | 请求方式错误 |
| `CommonJsonException` | `commonJsonExceptionHandler` | 自定义 | 业务异常 |
| `UnauthorizedException` | `unauthorizedExceptionHandler` | 403 | 权限不足 |
| `UnauthenticatedException` | `unauthenticatedException` | 401 | 未登录 |
| `MethodArgumentNotValidException` | `methodArgumentNotValidExceptionHandler` | 500 | 参数校验失败 |

### 异常处理示例

**1. 通用异常处理** (`GlobalExceptionHandler.java:29-33`):
```java
@ExceptionHandler(value = Exception.class)
public ApiResponse<?> defaultErrorHandler(HttpServletRequest req, Exception e) {
    logger.error("异常", e);
    return ApiResponse.error(500, "服务器异常，请稍后重试");
}
```

**2. 未登录异常处理** (`GlobalExceptionHandler.java:69-72`):
```java
@ExceptionHandler(org.example.config.exception.UnauthenticatedException.class)
public ApiResponse<?> unauthenticatedException() {
    return ApiResponse.error(401, "登录失效请重新登录");
}
```

**3. 权限不足异常处理** (`GlobalExceptionHandler.java:60-63`):
```java
@ExceptionHandler(org.example.config.exception.UnauthorizedException.class)
public ApiResponse<?> unauthorizedExceptionHandler() {
    return ApiResponse.error(403, "权限不符");
}
```

**4. 参数校验异常处理** (`GlobalExceptionHandler.java:77-87`):
```java
@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
public ApiResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
    Map<String, String> error = new HashMap<>();
    List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
    allErrors.forEach(er -> {
        String fieldName = ((FieldError) er).getField();
        String message = er.getDefaultMessage();
        error.put(fieldName, message);
    });
    return ApiResponse.error(error.toString());
}
```

### 抛出业务异常

在 Service 层使用 `CommonJsonException` 抛出业务异常：

```java
if (user == null) {
    throw new CommonJsonException("用户不存在");
}
```

## 配置说明

### application.yml

```yaml
spring:
  profiles:
    active: dev
```

### application-dev.yml

主要配置：
- 服务端口: `9046`
- MySQL 数据库: `192.168.10.141:33068/nblg_apply`
- Redis: `localhost:6379`
- RabbitMQ: `192.168.10.141:5672`
- 微信小程序 AppId 和 Secret

## 启动项目

```bash
# 编译项目
mvn clean package

# 运行项目
java -jar target/nbt_apply_demo.jar

# 或使用 Maven 直接运行
mvn spring-boot:run
```

启动成功后，访问 Swagger 文档：`http://localhost:9046/swagger-ui.html`

## 数据库

数据库初始化脚本：`all_table.sql`

## 日志

日志文件位置：`./logs/`

---

**最后更新**: 2026-03-24
