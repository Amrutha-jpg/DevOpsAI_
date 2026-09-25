package com.devopsai.backend.service;

import com.devopsai.backend.dto.SprintDto;
import com.devopsai.backend.dto.SprintRequest;
import com.devopsai.backend.entity.Project;
import com.devopsai.backend.entity.Sprint;
import com.devopsai.backend.entity.SprintStatus;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.repository.SprintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    public SprintService(SprintRepository sprintRepository, ProjectRepository projectRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public SprintDto createSprint(Long projectId, SprintRequest request) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Sprint sprint = new Sprint(
            request.getName(),
            request.getGoal(),
            request.getStartDate(),
            request.getEndDate(),
            project
        );

        Sprint savedSprint = sprintRepository.save(sprint);
        return new SprintDto(savedSprint);
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getSprintsForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return sprintRepository.findByProjectId(projectId).stream()
            .map(SprintDto::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public SprintDto updateSprintStatus(Long sprintId, SprintStatus status) {
        Sprint sprint = sprintRepository.findById(sprintId)
            .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        sprint.setStatus(status);
        Sprint updatedSprint = sprintRepository.save(sprint);
        return new SprintDto(updatedSprint);
    }
}
