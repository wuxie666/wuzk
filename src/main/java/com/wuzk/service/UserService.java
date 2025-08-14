package com.wuzk.service;

import com.wuzk.entity.User;

import java.util.List;

public interface UserService {
    User getUserById(Long id);
    List<User> getAllUsers();
    int createUser(User user);
    int updateUser(User user);
    int deleteUser(Long id);
    User getUserByUsername(String username); // 新增
}