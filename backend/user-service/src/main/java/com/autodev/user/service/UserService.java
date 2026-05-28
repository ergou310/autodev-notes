package com.autodev.user.service;

import com.autodev.user.dto.LoginRequest;
import com.autodev.user.dto.RegisterRequest;
import com.autodev.user.dto.UserDTO;

public interface UserService {

    /**
     * 用户注册
     */
    UserDTO register(RegisterRequest request);

    /**
     * 用户登录，返回 Sa-Token token
     */
    String login(LoginRequest request);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 根据ID获取用户
     */
    UserDTO getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    UserDTO getUserByUsername(String username);

    /**
     * 获取当前登录用户
     */
    UserDTO getCurrentUser();

    /**
     * 更新用户信息
     */
    UserDTO updateUser(Long id, UserDTO userDTO);
}
