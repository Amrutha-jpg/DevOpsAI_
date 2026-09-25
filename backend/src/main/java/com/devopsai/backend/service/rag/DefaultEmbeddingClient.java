package com.devopsai.backend.service.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultEmbeddingClient implements EmbeddingClient {

    private static final int VECTOR_DIMENSION = 64;

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[VECTOR_DIMENSION];
        }

        float[] vector = new float[VECTOR_DIMENSION];
        String[] tokens = text.toLowerCase().split("[^a-zA-Z0-9_]+");

        for (String token : tokens) {
            if (token.length() < 2) continue;
            int hash = Math.abs(token.hashCode());
            int index = hash % VECTOR_DIMENSION;
            vector[index] += 1.0f;
            // Also spread to adjacent dimension for semantic continuity
            vector[(index + 1) % VECTOR_DIMENSION] += 0.5f;
        }

        // L2 Normalize vector
        float normSquare = 0.0f;
        for (float val : vector) {
            normSquare += val * val;
        }
        float norm = (float) Math.sqrt(normSquare);
        if (norm > 0) {
            for (int i = 0; i < VECTOR_DIMENSION; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> results = new ArrayList<>();
        if (texts == null) return results;
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }
}
