package com.testit.reports.controller.dto;

import com.testit.reports.model.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    
    private Long id;
    
    @NotNull(message = "TestIT project ID is required")
    private UUID testitId;
    
    @NotBlank(message = "Project name is required")
    private String name;
    
    private String description;
    
    private Project.Status status;
    
    private boolean visible;
    
    /**
     * Convert Project entity to ProjectDto
     *
     * @param project Project entity
     * @param visible Visibility flag
     * @return ProjectDto
     */
    public static ProjectDto fromEntity(Project project, boolean visible) {
        return ProjectDto.builder()
                .id(project.getId())
                .testitId(project.getTestitId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .visible(visible)
                .build();
    }
    
    /**
     * Convert ProjectDto to Project entity
     *
     * @return Project entity
     */
    public Project toEntity() {
        Project project = new Project();
        project.setId(this.id);
        project.setTestitId(this.testitId);
        project.setName(this.name);
        project.setDescription(this.description);
        project.setStatus(this.status != null ? this.status : Project.Status.ACTIVE);
        return project;
    }
}
