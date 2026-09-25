package com.devopsai.backend.dto;

public class ContainerStatusDto {

    private String name;
    private String image;
    private String status; // RUNNING, HEALTHY, STARTING, STOPPED
    private String port;
    private String health;

    public ContainerStatusDto() {}

    public ContainerStatusDto(String name, String image, String status, String port, String health) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.port = port;
        this.health = health;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }
}
