package com.coinly.business.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coinly.business.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
