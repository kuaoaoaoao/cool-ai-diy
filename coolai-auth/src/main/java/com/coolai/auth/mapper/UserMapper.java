package com.coolai.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coolai.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
