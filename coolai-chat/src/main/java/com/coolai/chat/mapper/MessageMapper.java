package com.coolai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coolai.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
