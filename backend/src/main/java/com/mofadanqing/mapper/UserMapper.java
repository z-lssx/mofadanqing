package com.mofadanqing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mofadanqing.entity.User;

public interface UserMapper extends BaseMapper<User> {
    User selectByUsername(String username);
    User selectByPhone(String phone);
    User selectByEmail(String email);
}
