package com.coinly.business.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coinly.business.user.entity.UserEntity;
import com.coinly.business.user.mapper.UserMapper;
import com.coinly.business.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
}
