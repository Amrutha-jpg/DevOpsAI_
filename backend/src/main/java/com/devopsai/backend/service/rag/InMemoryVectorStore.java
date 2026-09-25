package com.devopsai.backend.service.rag;

import com.devopsai.backend.entity.CodebaseChunk;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryVectorStore implements VectorStore {

    private final Map<Long, List<CodebaseChunk>> store = new ConcurrentHashMap<>();

    @Override
    public void save(CodebaseChunk chunk) {
        store.computeIfAbsent(chunk.getProjectId(), id -> new ArrayList<>()).add(chunk);
    }

    @Override
    public void saveAll(List<CodebaseChunk> chunks) {
        for (CodebaseChunk chunk : chunks) {
            save(chunk);
        }
    }

    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he",
        "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with", "where", "how", "what", "which", "show"
    );

    @Override
    public List<CodebaseChunk> similaritySearch(Long projectId, float[] queryEmbedding, String queryText, int topK) {
        List<CodebaseChunk> chunks = store.getOrDefault(projectId, Collections.emptyList());
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }

        String[] queryTerms = queryText != null ? queryText.toLowerCase().split("[^a-zA-Z0-9_]+") : new String[0];

        Map<CodebaseChunk, Double> scoredChunks = new HashMap<>();
        for (CodebaseChunk chunk : chunks) {
            double keywordScore = 0.0;
            if (chunk.getContent() != null) {
                String contentLower = chunk.getContent().toLowerCase();
                String filenameLower = chunk.getFilename() != null ? chunk.getFilename().toLowerCase() : "";
                int matches = 0;
                for (String term : queryTerms) {
                    if (term.length() < 2 || STOP_WORDS.contains(term)) continue;
                    if (filenameLower.contains(term)) matches += 5;
                    if (contentLower.contains(term)) matches += 1;
                }
                keywordScore = Math.min(1.0, matches / 5.0);
            }
            scoredChunks.put(chunk, keywordScore);
        }

        return scoredChunks.entrySet().stream()
                .sorted(Map.Entry.<CodebaseChunk, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByProjectId(Long projectId) {
        store.remove(projectId);
    }

    @Override
    public long countByProjectId(Long projectId) {
        return store.getOrDefault(projectId, Collections.emptyList()).size();
    }
}
