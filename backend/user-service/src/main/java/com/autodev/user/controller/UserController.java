package com.autodev.user.controller;

import com.autodev.user.common.Result;
import com.autodev.user.dto.LoginRequest;
import com.autodev.user.dto.RegisterRequest;
import com.autodev.user.dto.UserDTO;
import com.autodev.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = userService.register(request);
        return Result.success(user);
    }

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        UserDTO user = userService.getCurrentUser();
        return Result.success(Map.of("token", token, "user", user));
    }

    /**
     * 退出登录
     * POST /api/user/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/me
     */
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * 根据ID获取用户
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 更新用户信息
     * PUT /api/user/{id}
     */
    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return Result.success(userService.updateUser(id, userDTO));
    }
}
