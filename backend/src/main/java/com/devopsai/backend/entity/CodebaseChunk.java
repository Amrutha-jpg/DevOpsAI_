package com.devopsai.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "codebase_chunks")
public class CodebaseChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String filename;

    @Column(name = "start_line", nullable = false)
    private int startLine;

    @Column(name = "end_line", nullable = false)
    private int endLine;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "chunk_type", nullable = false)
    private String chunkType; // SOURCE_CODE, COMMIT, PR_DIFF, DOCUMENTATION

    @Column(name = "embedding_json", columnDefinition = "TEXT")
    private String embeddingJson; // Serialized dense vector embedding (e.g. float array)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CodebaseChunk() {
    }

    public CodebaseChunk(Long projectId, String filename, int startLine, int endLine, String content, String chunkType) {
        this.projectId = projectId;
        this.filename = filename;
        this.startLine = startLine;
        this.endLine = endLine;
        this.content = content;
        this.chunkType = chunkType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChunkType() {
        return chunkType;
    }

    public void setChunkType(String chunkType) {
        this.chunkType = chunkType;
    }

    public String getEmbeddingJson() {
        return embeddingJson;
    }

    public void setEmbeddingJson(String embeddingJson) {
        this.embeddingJson = embeddingJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
