# ApiResponse 统一响应类使用说明

## 类位置
`org.example.common.ApiResponse<T>`

## 功能说明
统一 API 响应格式，让所有接口返回一致的数据结构。

## 响应结构
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 使用方法

### 1. 成功响应（带数据）
```java
@GetMapping("/user/{id}")
public ApiResponse<UserVO> getUser(@PathVariable Long id) {
    UserVO user = userService.getById(id);
    return ApiResponse.success(user);
}
```

### 2. 成功响应（无数据）
```java
@DeleteMapping("/user/{id}")
public ApiResponse<Void> deleteUser(@PathVariable Long id) {
    userService.removeById(id);
    return ApiResponse.success();
}
```

### 3. 成功响应（自定义消息）
```java
@PostMapping("/user")
public ApiResponse<UserVO> createUser(@RequestBody UserDTO userDTO) {
    UserVO user = userService.create(userDTO);
    return ApiResponse.success("创建成功", user);
}
```

### 4. 失败响应
```java
@PostMapping("/user")
public ApiResponse<UserVO> createUser(@RequestBody UserDTO userDTO) {
    if (userDTO.getName() == null) {
        return ApiResponse.error("用户名不能为空");
    }
    // ...
}
```

### 5. 失败响应（自定义错误码）
```java
public ApiResponse<UserVO> createUser(@RequestBody UserDTO userDTO) {
    if (userDTO.getName() == null) {
        return ApiResponse.error(400, "用户名不能为空");
    }
    // ...
}
```

## 完整 Controller 示例

```java
package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
public class UserController {

    @Operation(summary = "查询用户")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@PathVariable Long id) {
        UserVO user = userService.getById(id);
        return ApiResponse.success(user);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<UserVO> createUser(@RequestBody UserDTO dto) {
        UserVO user = userService.create(dto);
        return ApiResponse.success("创建成功", user);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<UserVO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        UserVO user = userService.update(id, dto);
        return ApiResponse.success(user);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return ApiResponse.success();
    }

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    public ApiResponse<List<UserVO>> listUsers() {
        List<UserVO> users = userService.list();
        return ApiResponse.success(users);
    }
}
```

## 注意事项

1. **泛型使用**：`<T>` 表示返回数据的类型
   - 返回对象：`ApiResponse<UserVO>`
   - 返回列表：`ApiResponse<List<UserVO>>`
   - 无返回数据：`ApiResponse<Void>`

2. **异常处理**：对于业务异常，仍然使用 `CommonJsonException` 抛出
   ```java
   if (user == null) {
       throw new CommonJsonException("用户不存在");
   }
   ```

3. **Swagger 文档**：配合 `@Schema` 注解，自动生成详细的 API 文档
   ```java
   @Schema(description = "用户信息")
   public class UserVO {
       @Schema(description = "用户ID")
       private Long id;

       @Schema(description = "用户名")
       private String name;
   }
   ```

## 优势

1. ✅ **统一响应格式**：所有接口返回结构一致
2. ✅ **类型安全**：泛型支持，编译时类型检查
3. ✅ **Swagger 友好**：自动识别响应数据类型
4. ✅ **便于维护**：集中管理响应逻辑
5. ✅ **代码复用**：所有 Controller 共享
