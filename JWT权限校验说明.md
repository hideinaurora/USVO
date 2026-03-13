# JWT Token 认证说明

## 认证机制

活动管理接口使用 JWT Token 进行身份认证和权限控制。

## Token 获取

### 1. 管理员登录获取 Token

**接口：** `POST /api/admin/login`

**请求示例：**
```bash
curl -X POST http://localhost:9013/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginName": "admin",
    "loginPassword": "123456",
    "captchaKey": "验证码key",
    "captchaCode": "验证码"
  }'
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzZWVkIjoie1wiYWNjb3VudElkXCI6MSxcInJvbGVJZFwiOjF9IiwiaWF0IjoxNzQxOTk1MjAwLCJleHAiOjE3NDIwMDI0MDB9.xxx...",
    "userId": 1,
    "loginName": "admin",
    "roleId": 1
  }
}
```

## Token 使用

### 请求头设置

所有需要认证的接口都需要在请求头中携带 `token` 字段：

```bash
token: eyJhbGciOiJIUzI1NiJ9.eyJzZWVkIjoie1wiYWNjb3VudElkXCI6MSxcInJvbGVJZFwiOjF9IiwiaWF0IjoxNzQxOTk1MjAwLCJleHAiOjE3NDIwMDI0MDB9.xxx...
```

### Postman 设置

1. 打开 Postman
2. 在 **Headers** 标签页添加：
   - Key: `token`
   - Value: `你的JWT令牌`

### curl 示例

```bash
curl -X POST http://localhost:9013/api/apply/page \
  -H "Content-Type: application/json" \
  -H "token: eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "pageNum": 1,
    "pageSize": 10
  }'
```

## 权限说明

### 角色类型

| 角色 | roleId | 说明 |
|------|--------|------|
| ADMIN | 1 | 管理员 |
| USER | 2 | 普通用户 |

### 接口权限配置

| 接口 | 权限码 | 允许角色 | 说明 |
|------|--------|---------|------|
| `POST /api/apply/page` | apply:page | ADMIN | 分页查询活动（仅管理员） |
| `GET /api/apply/list` | apply:list | ADMIN | 查询活动列表（仅管理员） |
| `GET /api/apply/{applyId}` | apply:detail | ADMIN, USER | 查询活动详情（管理员和用户） |
| `POST /api/apply/add` | apply:add | ADMIN | 新增活动（仅管理员） |
| `PUT /api/apply/update` | apply:update | ADMIN | 修改活动（仅管理员） |
| `DELETE /api/apply/{applyIds}` | apply:delete | ADMIN | 删除活动（仅管理员） |

## Token 有效期

- Token 有效期：**720 分钟**（12 小时）
- 过期后需要重新登录获取新 Token

## 错误处理

### 1. 未登录（Token 缺失或无效）

**响应示例：**
```json
{
  "longResult": -1,
  "objResult": "登录失效请重新登录"
}
```

### 2. 权限不足

**响应示例：**
```json
{
  "longResult": -1,
  "objResult": "权限不符"
}
```

### 3. Token 过期

**响应示例：**
```json
{
  "longResult": -1,
  "objResult": "登录失效请重新登录"
}
```

## 完整示例

### 1. 登录获取 Token

```bash
# 步骤1：获取验证码
curl -X GET http://localhost:9013/api/admin/captcha

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "captchaImage": "data:image/png;base64,..."
  }
}

# 步骤2：登录
curl -X POST http://localhost:9013/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginName": "admin",
    "loginPassword": "123456",
    "captchaKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "captchaCode": "A3b9"
  }'

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "loginName": "admin",
    "roleId": 1
  }
}
```

### 2. 使用 Token 访问接口

```bash
# 分页查询活动
curl -X POST http://localhost:9013/api/apply/page \
  -H "Content-Type: application/json" \
  -H "token: eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "pageNum": 1,
    "pageSize": 10
  }'
```

## Swagger 文档测试

在 Swagger UI 中测试需要认证的接口：

1. 访问 http://localhost:9013/swagger-ui.html
2. 点击右上角 **Authorize** 按钮
3. 在弹出的对话框中输入 Token（不需要加 `Bearer ` 前缀）
4. 点击 **Authorize** 确认
5. 现在可以测试所有需要认证的接口了

**注意：** Swagger 中的认证可能需要手动设置请求头，建议使用 Postman 或 Apifox 进行测试。

## @RequiresPermissions 注解说明

### 注解属性

```java
@RequiresPermissions(
    value = "apply:add",              // 权限码（用于日志记录等）
    apiAuth = {ApiAuth.ADMIN}         // 允许的角色列表
)
```

### 使用示例

```java
// 仅管理员可访问
@RequiresPermissions(value = "apply:add", apiAuth = {ApiAuth.ADMIN})

// 管理员和用户都可访问
@RequiresPermissions(value = "apply:detail", apiAuth = {ApiAuth.ADMIN, ApiAuth.USER})

// 仅用户可访问
@RequiresPermissions(value = "user:profile", apiAuth = {ApiAuth.USER})
```

## 安全建议

1. **Token 存储**：前端建议将 Token 存储在 localStorage 或 sessionStorage 中
2. **Token 传递**：每次请求都在请求头中携带 Token
3. **Token 刷新**：建议在 Token 快过期时提前刷新
4. **退出登录**：退出登录时清除本地存储的 Token
5. **HTTPS**：生产环境务必使用 HTTPS 传输，防止 Token 被窃取
