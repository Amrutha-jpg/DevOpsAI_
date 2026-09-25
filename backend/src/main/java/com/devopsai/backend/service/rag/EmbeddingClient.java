package com.devopsai.backend.service.rag;

import java.util.List;

public interface EmbeddingClient {
    float[] embed(String text);
    List<float[]> embedBatch(List<String> texts);
}
