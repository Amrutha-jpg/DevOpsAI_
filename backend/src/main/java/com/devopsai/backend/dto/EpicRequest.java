package com.devopsai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EpicRequest {

    @NotBlank(message = "Epic name is required")
    @Size(min = 2, max = 100, message = "Epic name must be between 2 and 100 characters")
    private String name;

    private String description;

    public EpicRequest() {
    }

    public EpicRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
