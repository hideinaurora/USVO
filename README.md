# 高校场馆场地预约系统 API 文档

## 项目简介

本项目是一个基于 Spring Boot 的高校场馆场地预约系统，提供管理端和移动端两套 API 接口。系统支持场馆管理、场地预约、时间片管理、押金支付、人脸识别签到、违约记录等功能，并使用分布式锁保证高并发场景下的场地预约管理。

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
| Face++ | - | 人脸识别服务 |
| Alibaba Cloud OSS | - | 对象存储服务 |

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
│   ├── FaceServiceConfig.java   # 人脸识别服务配置
│   ├── MybatisPlusConfig.java   # MyBatis Plus 配置
│   ├── RedisConfig.java         # Redis 配置
│   └── SwaggerConfig.java       # Swagger 配置
├── controller/                   # 控制器层
│   ├── AdminController.java     # 管理端用户接口
│   ├── AppController.java       # 移动端用户接口
│   ├── ApplyController.java     # 活动管理接口（已停用）
│   ├── BookingController.java   # 预约模块接口
│   ├── CourtController.java     # 场地时间片接口
│   ├── DepositPaymentController.java # 押金支付接口
│   ├── DifyController.java      # Dify智能体查询接口（已停用）
│   ├── FaceController.java      # 人脸识别接口
│   ├── MqController.java        # 消息队列回调接口（已停用）
│   ├── PaymentCallbackController.java # 支付回调接口
│   ├── RefundController.java    # 退款管理接口
│   ├── UploadController.java    # 文件上传接口
│   ├── VenueController.java     # 场馆模块接口
│   └── WechatController.java    # 微信接口
├── dto/                          # 数据传输对象
├── entity/                       # 实体类
│   ├── activity/                # 活动相关实体（已停用）
│   ├── basic/                   # 基础实体（用户、违约记录）
│   ├── booking/                 # 预约相关实体（预约、时间片、支付、签到日志）
│   ├── failed/                  # 失败记录实体
│   └── sys/                     # 系统实体（管理员）
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

1. **管理端用户** - 管理员登录、验证码、令牌刷新、用户管理、场馆管理、场地管理、预约管理、时间片管理
2. **移动端用户** - 用户注册、登录、信息查询、违约记录查询、退款申请
3. **场馆模块** - 场馆列表、详情、按类型/关键词筛选、距离排序
4. **场地时间片** - 可预约时间片查询
5. **预约模块** - 创建预约、取消、列表、详情、签到
6. **押金支付** - 预约押金支付
7. **人脸识别模块** - 人脸特征提取、人脸验证
8. **微信接口** - 微信小程序登录
9. **退款管理** - 退款审核、查询
10. **支付回调** - 支付结果通知
11. **文件上传** - 阿里云OSS文件上传

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
| `/api/admin/user/list` | GET | 获取用户列表 | ADMIN |
| `/api/admin/user/{userId}` | DELETE | 删除用户 | ADMIN |
| `/api/admin/user/{userId}/status` | PUT | 禁用/启用用户 | ADMIN |
| `/api/admin/venue/list` | GET | 获取场馆列表 | ADMIN |
| `/api/admin/venue` | POST | 添加场馆 | ADMIN |
| `/api/admin/venue/{venueId}` | PUT | 修改场馆 | ADMIN |
| `/api/admin/venue/{venueId}` | DELETE | 删除场馆 | ADMIN |
| `/api/admin/court/list` | GET | 获取场地列表 | ADMIN |
| `/api/admin/court` | POST | 添加场地 | ADMIN |
| `/api/admin/court/{courtId}` | PUT | 修改场地 | ADMIN |
| `/api/admin/court/{courtId}` | DELETE | 删除场地 | ADMIN |
| `/api/admin/booking/list` | GET | 获取预约列表 | ADMIN |
| `/api/admin/booking/{bookingId}` | DELETE | 删除预约 | ADMIN |
| `/api/admin/booking/{bookingId}/force-cancel` | POST | 强制取消预约 | ADMIN |
| `/api/admin/timeslot/generate` | POST | 生成时间片 | ADMIN |
| `/api/admin/timeslot/list` | GET | 时间片列表查询 | ADMIN |
| `/api/admin/timeslot/{slotId}` | PUT | 更新时间片 | ADMIN |
| `/api/admin/timeslot/{slotId}` | DELETE | 删除时间片 | ADMIN |
| `/api/admin/timeslot/batch` | DELETE | 批量删除时间片 | ADMIN |
| `/api/admin/timeslot/{slotId}/lock` | PUT | 锁定/解锁时间片 | ADMIN |

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
| `/api/app/info` | GET | 获取用户信息 | USER |
| `/api/app/info` | PUT | 更新用户信息 | USER |
| `/api/app/violations` | GET | 获取违约记录 | USER |
| `/api/app/refund/apply` | POST | 申请退款 | USER |

### 场馆模块接口（`/api/venue`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/venue/list` | GET | 获取场馆列表（支持按类型、关键词筛选，距离排序） | 公开 |
| `/api/venue/page` | GET | 分页获取场馆列表 | 公开 |
| `/api/venue/{venueId}` | GET | 获取场馆详情（包含场地列表） | 公开 |

**查询场馆示例**：
```
GET /api/venue/list?type=basketball&keyword=体育馆&latitude=30.5728&longitude=104.0668
```

### 场地时间片接口（`/api/court`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/court/{courtId}/slots` | GET | 获取可预约时间片 | 公开 |
| `/api/court/available-slots` | GET | 获取可预约时间（批量） | 公开 |

### 预约模块接口（`/api/booking`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/booking/create` | POST | 创建预约 | USER |
| `/api/booking/cancel` | POST | 取消预约 | USER |
| `/api/booking/list` | GET | 获取我的预约记录 | USER |
| `/api/booking/{bookingId}` | GET | 获取预约详情 | USER |
| `/api/booking/checkin` | POST | 签到入场 | USER |

**创建预约示例**：
```json
POST /api/booking/create
Authorization: <token>
{
  "courtId": 1,
  "startTime": "2026-07-28 14:00:00",
  "endTime": "2026-07-28 16:00:00"
}
```

### 押金支付接口（`/api/payment`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/payment/pay` | POST | 支付押金 | USER |

### 人脸识别接口（`/api/face`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/face/extract` | POST | 提取人脸特征 | USER |
| `/api/face/verify` | POST | 验证人脸 | USER |
| `/api/face/health` | GET | 检查人脸服务状态 | 公开 |

### 文件上传接口（`/api/upload`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/upload` | POST | 文件上传（阿里云OSS） | 公开 |

### 微信接口（`/api/wechat`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/wechat/login/code` | POST | 微信小程序登录 | 公开 |

### 退款管理接口（`/api/refund`）

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/refund/page` | POST | 分页查询退款列表 | ADMIN |
| `/api/refund/list` | GET | 查询退款列表（不分页） | ADMIN |
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
@PostMapping("/create")
@RequiresPermissions(value = "booking:create", apiAuth = {ApiAuth.USER})
public ApiResponse<BookingCreateResultVO> create(@Valid @RequestBody BookingCreateDTO dto) {
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
- MySQL 数据库: `127.0.0.1:3306/nblg_apply`
- Redis: `localhost:6379`
- RabbitMQ: `localhost:5672`
- 人脸识别服务: `http://localhost:5000`
- 微信小程序 AppId 和 Secret
- 阿里云 OSS 配置

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

数据库初始化脚本：`sql/all_table.sql`

## 日志

日志文件位置：`./logs/`

---

**最后更新**: 2026-06-11