package com.example.demo.service;

import com.example.demo.entity.User;
import java.util.List;

public interface UserService {

    User saveUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    User getUserByEmail(String email);

    /**
     * 复杂调试场景：包含多次 IO 读写与对象修改
     */
    User complexUserFlow(Long id);
}