package com.testit.reports.controller;

import com.testit.reports.controller.dto.ApiResponse;
import com.testit.reports.controller.dto.ProjectDto;
import com.testit.reports.model.entity.Project;
import com.testit.reports.model.entity.User;
import com.testit.reports.model.entity.UserProject;
import com.testit.reports.repository.UserProjectRepository;
import com.testit.reports.service.ProjectService;
import com.testit.reports.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final UserProjectRepository userProjectRepository;

    /**
     * Get all projects visible to current user
     *
     * @param userDetails Current user details
     * @return List of projects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getVisibleProjects(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Getting visible projects for user: {}", userDetails.getUsername());
            
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            log.info("User found: {}, role: {}", user.getUsername(), user.getRole());

            List<Project> projects;
            if (user.getRole() == User.Role.ADMIN) {
                // Admin can see all projects
                log.info("User is admin, getting all projects");
                projects = projectService.getAllProjects();
            } else {
                // Regular user can see only visible projects
                log.info("User is not admin, getting visible projects for user ID: {}", user.getId());
                projects = projectService.getVisibleProjectsByUserId(user.getId());
            }
            
            log.info("Found {} projects", projects.size());

            List<ProjectDto> projectDtos = projects.stream()
                    .map(project -> ProjectDto.fromEntity(project, true))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(projectDtos));
        } catch (Exception e) {
            log.error("Error getting visible projects", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all projects assigned to current user (visible and hidden)
     *
     * @param userDetails Current user details
     * @return List of projects
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getAllUserProjects(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            List<Project> projects;
            if (user.getRole() == User.Role.ADMIN) {
                // Admin can see all projects
                projects = projectService.getAllProjects();
            } else {
                // Regular user can see only assigned projects
                projects = projectService.getAllProjectsByUserId(user.getId());
            }

            // For each project, check if it's visible to the user
            List<ProjectDto> projectDtos = projects.stream()
                    .map(project -> {
                        boolean visible = true;
                        if (user.getRole() != User.Role.ADMIN) {
                            // Check if project is visible to user
                            visible = project.getUserProjects().stream()
                                    .filter(up -> up.getUser().getId().equals(user.getId()))
                                    .findFirst()
                                    .map(UserProject::isVisible)
                                    .orElse(false);
                        }
                        return ProjectDto.fromEntity(project, visible);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(projectDtos));
        } catch (Exception e) {
            log.error("Error getting all user projects", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get project by ID
     *
     * @param id          Project ID
     * @param userDetails Current user details
     * @return Project
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Project project = projectService.getProjectById(id);

            // Check if user has access to project
            boolean hasAccess = user.getRole() == User.Role.ADMIN ||
                    projectService.getAllProjectsByUserId(user.getId()).stream()
                            .anyMatch(p -> p.getId().equals(id));

            if (!hasAccess) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }

            // Check if project is visible to user
            boolean visible = user.getRole() == User.Role.ADMIN ||
                    project.getUserProjects().stream()
                            .filter(up -> up.getUser().getId().equals(user.getId()))
                            .findFirst()
                            .map(UserProject::isVisible)
                            .orElse(false);

            return ResponseEntity.ok(ApiResponse.success(ProjectDto.fromEntity(project, visible)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting project by ID", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Add project from TestIT
     *
     * @param testitId    TestIT project ID (can be UUID or numeric)
     * @param userDetails Current user details
     * @return Added project
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ProjectDto>> addProjectFromTestIt(
            @RequestParam String testitId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Adding project from TestIT with ID: {}", testitId);
            
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Project project = projectService.addProjectFromTestIt(testitId, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Project added successfully", ProjectDto.fromEntity(project, true)));
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument when adding project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Data integrity violation when adding project: {}", e.getMessage());
            
            // Check if it's a duplicate key violation
            if (e.getMessage() != null && e.getMessage().contains("duplicate key") && e.getMessage().contains("projects_testit_id_key")) {
                log.info("Duplicate project ID detected, trying to add existing project to user");
                
                try {
                    // Extract the UUID from the error message using regex
                    String errorMsg = e.getMessage();
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\(testit_id\\)=\\(([^)]+)\\)");
                    java.util.regex.Matcher matcher = pattern.matcher(errorMsg);
                    
                    if (matcher.find()) {
                        String extractedId = matcher.group(1);
                        log.info("Extracted ID from error message using regex: {}", extractedId);
                        
                        // Try to get the user again
                        User user = userService.getUserByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                        
                        // Try to find the project with this ID
                        UUID projectUuid = UUID.fromString(extractedId);
                        Project existingProject = projectService.getProjectByTestitId(projectUuid);
                        
                        // Check if user already has this project
                        if (existingProject != null) {
                            // Add project to user if they don't already have it
                            if (!projectService.getAllProjectsByUserId(user.getId()).contains(existingProject)) {
                                UserProject userProject = new UserProject();
                                userProject.setUser(user);
                                userProject.setProject(existingProject);
                                userProject.setVisible(true);
                                userProjectRepository.save(userProject);
                                
                                return ResponseEntity.ok(ApiResponse.success("Project added successfully", 
                                        ProjectDto.fromEntity(existingProject, true)));
                            } else {
                                return ResponseEntity.badRequest().body(ApiResponse.error("User already has this project"));
                            }
                        }
                    }
                } catch (Exception recoveryEx) {
                    log.error("Error during recovery attempt: {}", recoveryEx.getMessage());
                }
                
                return ResponseEntity.badRequest().body(ApiResponse.error("Project with this ID already exists but could not be added to your account. Please contact an administrator."));
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error("Error adding project: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding project from TestIT", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error adding project from TestIT"));
        }
    }

    /**
     * Update project visibility for current user
     *
     * @param id          Project ID
     * @param visible     Visibility flag
     * @param userDetails Current user details
     * @return Success response
     */
    @PutMapping("/{id}/visibility")
    public ResponseEntity<ApiResponse<Void>> updateProjectVisibility(
            @PathVariable Long id,
            @RequestParam boolean visible,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            projectService.updateProjectVisibility(user.getId(), id, visible);
            return ResponseEntity.ok(ApiResponse.success("Project visibility updated successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating project visibility", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Remove project from current user
     *
     * @param id          Project ID
     * @param userDetails Current user details
     * @return Success response
     */
    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<Void>> removeProjectFromUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            projectService.removeProjectFromUser(user.getId(), id);
            return ResponseEntity.ok(ApiResponse.success("Project removed successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error removing project from user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update project status (admin only)
     *
     * @param id     Project ID
     * @param status New status
     * @return Updated project
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProjectStatus(
            @PathVariable Long id,
            @RequestParam Project.Status status) {
        try {
            Project project = projectService.updateProjectStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Project status updated successfully", ProjectDto.fromEntity(project, true)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating project status", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete project (admin only)
     *
     * @param id Project ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting project", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
