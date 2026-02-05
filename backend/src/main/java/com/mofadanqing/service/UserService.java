package com.mofadanqing.service;

import com.mofadanqing.entity.User;

public interface UserService {
    User login(String username, String password);
    User loginByPhone(String phone, String smsCode);
    User register(User user);
    User getById(Long id);
    User getByUsername(String username);
    User getByPhone(String phone);
    User getByEmail(String email);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void updateEmail(Long userId, String newEmail, String code);
    void updatePhone(Long userId, String newPhone, String code);
}
