package com.coolai.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coolai.common.exception.BusinessException;
import com.coolai.common.model.PageResult;
import com.coolai.knowledge.mapper.DocumentMapper;
import com.coolai.knowledge.mapper.KnowledgeBaseMapper;
import com.coolai.knowledge.mapper.ChunkMapper;
import com.coolai.model.entity.Document;
import com.coolai.model.entity.KnowledgeBase;
import com.coolai.model.entity.Chunk;
import com.coolai.model.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Knowledge Base CRUD Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final DocumentMapper documentMapper;
    private final ChunkMapper chunkMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public KnowledgeBase createKnowledgeBase(String userId, String name, String description) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setUserId(userId);
        kb.setName(name);
        kb.setDescription(description);
        kb.setConfig(new HashMap<>());
        kb.setChunkCount(0);
        kb.setDocCount(0);
        kb.setCreatedAt(LocalDateTime.now());
        kb.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.insert(kb);
        return kb;
    }

    public PageResult<KnowledgeBase> listKnowledgeBases(String userId, int page, int pageSize) {
        Page<KnowledgeBase> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getUserId, userId)
                .orderByDesc(KnowledgeBase::getUpdatedAt);
        Page<KnowledgeBase> result = knowledgeBaseMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public KnowledgeBase getKnowledgeBase(String kbId, String userId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || !kb.getUserId().equals(userId)) {
            throw BusinessException.notFound("Knowledge base not found");
        }
        return kb;
    }

    @Transactional
    public void deleteKnowledgeBase(String kbId, String userId) {
        KnowledgeBase kb = getKnowledgeBase(kbId, userId);
        // Delete all chunks
        chunkMapper.delete(new LambdaQueryWrapper<Chunk>()
                .in(Chunk::getDocId,
                        documentMapper.selectList(new LambdaQueryWrapper<Document>()
                                .eq(Document::getKbId, kbId))
                                .stream().map(Document::getId).toList()));
        // Delete all documents
        documentMapper.delete(new LambdaQueryWrapper<Document>().eq(Document::getKbId, kbId));
        // Delete knowledge base
        knowledgeBaseMapper.deleteById(kbId);
        log.info("Deleted knowledge base: {}", kbId);
    }

    public PageResult<Document> listDocuments(String kbId, String userId, int page, int pageSize) {
        getKnowledgeBase(kbId, userId); // Verify ownership
        Page<Document> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<Document>()
                .eq(Document::getKbId, kbId)
                .orderByDesc(Document::getCreatedAt);
        Page<Document> result = documentMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Transactional
    public void deleteDocument(String docId, String userId) {
        Document doc = documentMapper.selectById(docId);
        if (doc == null) {
            throw BusinessException.notFound("Document not found");
        }
        // Verify ownership through knowledge base
        getKnowledgeBase(doc.getKbId(), userId);
        // Delete chunks
        chunkMapper.delete(new LambdaQueryWrapper<Chunk>().eq(Chunk::getDocId, docId));
        // Delete document
        documentMapper.deleteById(docId);
        log.info("Deleted document: {}", docId);
    }
}
