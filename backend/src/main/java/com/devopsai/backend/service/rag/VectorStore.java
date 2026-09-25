package com.devopsai.backend.service.rag;

import com.devopsai.backend.entity.CodebaseChunk;

import java.util.List;

public interface VectorStore {
    void save(CodebaseChunk chunk);
    void saveAll(List<CodebaseChunk> chunks);
    List<CodebaseChunk> similaritySearch(Long projectId, float[] queryEmbedding, String queryText, int topK);
    void deleteByProjectId(Long projectId);
    long countByProjectId(Long projectId);
}
