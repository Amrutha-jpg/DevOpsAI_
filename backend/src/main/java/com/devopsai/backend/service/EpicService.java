package com.devopsai.backend.service;

import com.devopsai.backend.dto.EpicDto;
import com.devopsai.backend.dto.EpicRequest;
import com.devopsai.backend.entity.Epic;
import com.devopsai.backend.entity.Project;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.EpicRepository;
import com.devopsai.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EpicService {

    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;

    public EpicService(EpicRepository epicRepository, ProjectRepository projectRepository) {
        this.epicRepository = epicRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public EpicDto createEpic(Long projectId, EpicRequest request) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Epic epic = new Epic(request.getName(), request.getDescription(), project);
        Epic savedEpic = epicRepository.save(epic);
        return new EpicDto(savedEpic);
    }

    @Transactional(readOnly = true)
    public List<EpicDto> getEpicsForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return epicRepository.findByProjectId(projectId).stream()
            .map(EpicDto::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteEpic(Long epicId) {
        Epic epic = epicRepository.findById(epicId)
            .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + epicId));
        epicRepository.delete(epic);
    }
}
