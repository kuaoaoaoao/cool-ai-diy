package com.coolai.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coolai.chat.mapper.ConversationMapper;
import com.coolai.common.exception.BusinessException;
import com.coolai.common.model.PageResult;
import com.coolai.model.entity.Conversation;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;

    @Transactional
    public Conversation createConversation(String userId, String title, String model) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "New Conversation");
        conversation.setModel(model);
        conversation.setConfig(new HashMap<>());
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        conversationMapper.insert(conversation);
        return conversation;
    }

    @Cacheable(value = "conversations", key = "#userId + ':' + #page + ':' + #pageSize")
    public PageResult<Conversation> listConversations(String userId, int page, int pageSize) {
        Page<Conversation> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdatedAt);

        Page<Conversation> result = conversationMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public Conversation getConversation(String conversationId, String userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw BusinessException.notFound("Conversation not found");
        }
        return conversation;
    }

    @CacheEvict(value = "conversations", key = "#userId + ':' + '*'")
    @Transactional
    public void deleteConversation(String conversationId, String userId) {
        Conversation conversation = getConversation(conversationId, userId);
        conversationMapper.deleteById(conversation.getId());
    }
}
