package com.wuzk.controller;

import com.wuzk.entity.User;
import com.wuzk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户的增删改查接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查询所有用户", description = "获取系统中所有用户列表")
    @Tool(description = "查询所有用户")
    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "根据ID查询用户", description = "通过用户ID获取单个用户信息")
    @Tool(description = "通过用户ID获取单个用户信息")
    @GetMapping("/{id}")
    public User getUserById(@ToolParam(description = "用户ID") @Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "创建用户", description = "创建一个新的用户")
    @Tool(description = "创建一个新的用户")
    @PostMapping
    public Map<String, Object> createUser(@ToolParam(description = "用户实体") @RequestBody User user) {
        userService.createUser(user);

        var response = new HashMap<String, Object>();
        response.put("status", "success");
        response.put("message", "User created");
        response.put("user", user);
        return response;
    }


    @Operation(summary = "更新用户", description = "更新已有用户信息")
    @Tool(description = "更新已有用户信息")
    @PutMapping
    public Map<String, Object> updateUser(@ToolParam(description = "用户实体") @RequestBody User user) {
        userService.updateUser(user);

        var response = new HashMap<String, Object>();
        response.put("status", "success");
        response.put("message", "User updated");
        response.put("user", user);
        return response;
    }

    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @Tool(description = "根据用户ID删除用户")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@ToolParam(description = "用户ID") @Parameter(description = "用户ID")  @PathVariable Long id) {
        userService.deleteUser(id);
        var response = new HashMap<String, Object>();
        response.put("status", "success");
        response.put("message", "User deleted");
        response.put("userId", id);
        return response;
    }


}