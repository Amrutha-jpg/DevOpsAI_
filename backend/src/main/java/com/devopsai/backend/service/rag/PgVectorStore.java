package com.devopsai.backend.service.rag;

import com.devopsai.backend.entity.CodebaseChunk;
import com.devopsai.backend.repository.CodebaseChunkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Primary
public class PgVectorStore implements VectorStore {

    private final CodebaseChunkRepository repository;
    private final ObjectMapper objectMapper;

    public PgVectorStore(CodebaseChunkRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(CodebaseChunk chunk) {
        repository.save(chunk);
    }

    @Override
    public void saveAll(List<CodebaseChunk> chunks) {
        repository.saveAll(chunks);
    }

    @Override
    public List<CodebaseChunk> similaritySearch(Long projectId, float[] queryEmbedding, String queryText, int topK) {
        List<CodebaseChunk> chunks = repository.findByProjectId(projectId);
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }

        // Hybrid search: Calculate Cosine Similarity + Keyword Match Score
        Map<CodebaseChunk, Double> scoredChunks = new HashMap<>();
        String[] queryTerms = queryText != null ? queryText.toLowerCase().split("[^a-zA-Z0-9_]+") : new String[0];

        for (CodebaseChunk chunk : chunks) {
            double vectorScore = 0.0;
            if (queryEmbedding != null && chunk.getEmbeddingJson() != null) {
                try {
                    float[] chunkEmbedding = objectMapper.readValue(chunk.getEmbeddingJson(), float[].class);
                    vectorScore = calculateCosineSimilarity(queryEmbedding, chunkEmbedding);
                } catch (Exception ignored) {
                }
            }

            // Keyword match score
            double keywordScore = 0.0;
            if (queryTerms.length > 0 && chunk.getContent() != null) {
                String contentLower = chunk.getContent().toLowerCase();
                String filenameLower = chunk.getFilename() != null ? chunk.getFilename().toLowerCase() : "";
                int matches = 0;
                for (String term : queryTerms) {
                    if (term.length() < 2) continue;
                    if (filenameLower.contains(term)) matches += 3;
                    if (contentLower.contains(term)) matches += 1;
                }
                keywordScore = Math.min(1.0, matches / 5.0);
            }

            // Weighted Hybrid Score: 60% Dense Vector + 40% Keyword Similarity
            double totalScore = (0.60 * vectorScore) + (0.40 * keywordScore);
            scoredChunks.put(chunk, totalScore);
        }

        return scoredChunks.entrySet().stream()
                .sorted(Map.Entry.<CodebaseChunk, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByProjectId(Long projectId) {
        repository.deleteByProjectId(projectId);
    }

    @Override
    public long countByProjectId(Long projectId) {
        return repository.countByProjectId(projectId);
    }

    private double calculateCosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
