package com.testit.reports.service;

import com.testit.reports.client.testit.TestItApiClient;
import com.testit.reports.client.testit.dto.ProjectDto;
import com.testit.reports.model.entity.Project;
import com.testit.reports.model.entity.User;
import com.testit.reports.model.entity.UserProject;
import com.testit.reports.repository.ProjectRepository;
import com.testit.reports.repository.TestCaseStatisticsRepository;
import com.testit.reports.repository.TestPointResultRepository;
import com.testit.reports.repository.TestRunStatisticsRepository;
import com.testit.reports.repository.UserProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final TestCaseStatisticsRepository testCaseStatisticsRepository;
    private final TestRunStatisticsRepository testRunStatisticsRepository;
    private final TestPointResultRepository testPointResultRepository;
    private final UserService userService;
    private final TestItApiClient testItApiClient;
    private final GlobalSettingService globalSettingService;

    /**
     * Get all projects
     *
     * @return List of all projects
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Get all active projects
     *
     * @return List of all active projects
     */
    public List<Project> getAllActiveProjects() {
        return projectRepository.findAllActive();
    }

    /**
     * Get project by ID
     *
     * @param id Project ID
     * @return Project
     * @throws EntityNotFoundException if project not found
     */
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }

    /**
     * Get project by TestIT ID
     *
     * @param testitId TestIT project ID
     * @return Project
     * @throws EntityNotFoundException if project not found
     */
    public Project getProjectByTestitId(UUID testitId) {
        return projectRepository.findByTestitId(testitId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with TestIT id: " + testitId));
    }

    /**
     * Get projects visible to user
     *
     * @param userId User ID
     * @return List of projects visible to user
     */
    public List<Project> getVisibleProjectsByUserId(Long userId) {
        return projectRepository.findVisibleProjectsByUserId(userId);
    }

    /**
     * Get all projects assigned to user (visible and hidden)
     *
     * @param userId User ID
     * @return List of all projects assigned to user
     */
    public List<Project> getAllProjectsByUserId(Long userId) {
        return projectRepository.findAllProjectsByUserId(userId);
    }

    /**
     * Add project from TestIT
     *
     * @param testitId TestIT project ID (can be UUID or numeric)
     * @param userId   User ID
     * @return Added project
     * @throws IllegalArgumentException if project already exists
     */
    @Transactional
    public Project addProjectFromTestIt(String testitId, Long userId) {
        log.info("Adding project from TestIT with ID: {}", testitId);
        
        // Try to parse as UUID first
        UUID testitUuid = null;
        try {
            testitUuid = UUID.fromString(testitId);
            log.info("Parsed ID as UUID: {}", testitUuid);
        } catch (IllegalArgumentException e) {
            log.info("ID is not a UUID, will try as numeric ID: {}", testitId);
        }
        
        // Check if project already exists with this UUID
        if (testitUuid != null) {
            try {
                // Try to find project by UUID
                Project existingProject = getProjectByTestitId(testitUuid);
                log.info("Found existing project with UUID: {}", testitUuid);
                
                // Check if user already has this project
                if (userProjectRepository.existsByUserIdAndProjectId(userId, existingProject.getId())) {
                    throw new IllegalArgumentException("User already has this project");
                }
                
                // Add project to user
                User user = userService.getUserById(userId);
                UserProject userProject = new UserProject();
                userProject.setUser(user);
                userProject.setProject(existingProject);
                userProject.setVisible(true);
                userProjectRepository.save(userProject);
                
                log.info("Added existing project to user: {}", userId);
                return existingProject;
            } catch (EntityNotFoundException e) {
                log.info("Project with UUID {} not found, will create new", testitUuid);
                // Project not found, continue with creation
            }
        }
        
        // If we get here, we need to check all projects to see if any match the ID
        // This is needed for numeric IDs or when the UUID wasn't found
        List<Project> allProjects = projectRepository.findAll();
        for (Project project : allProjects) {
            // Convert project's UUID to string and compare with the requested ID
            if (project.getTestitId().toString().equals(testitId)) {
                log.info("Found existing project with ID (string comparison): {}", testitId);
                
                // Check if user already has this project
                if (userProjectRepository.existsByUserIdAndProjectId(userId, project.getId())) {
                    throw new IllegalArgumentException("User already has this project");
                }
                
                // Add project to user
                User user = userService.getUserById(userId);
                UserProject userProject = new UserProject();
                userProject.setUser(user);
                userProject.setProject(project);
                userProject.setVisible(true);
                userProjectRepository.save(userProject);
                
                log.info("Added existing project to user: {}", userId);
                return project;
            }
        }

        // Get project details from TestIT
        User user = userService.getUserById(userId);
        String token = user.getTestitToken();
        if (token == null || token.isEmpty()) {
            token = globalSettingService.getGlobalTestItToken();
        }

        ProjectDto projectDto;
        if (testitUuid != null) {
            // If UUID, use the existing method
            projectDto = testItApiClient.getProject(token, testitUuid).block();
        } else {
            // If numeric, use the new method
            try {
                // Try to parse as Long
                Long numericId = Long.parseLong(testitId);
                projectDto = testItApiClient.getProjectById(token, numericId).block();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid project ID format. Must be UUID or numeric: " + testitId);
            }
        }
        
        if (projectDto == null) {
            throw new IllegalArgumentException("Project not found in TestIT with id: " + testitId);
        }

        // Create new project
        Project project = new Project();
        
        // Convert ID to UUID if it's not already a UUID
        UUID projectUuid;
        if (projectDto.getId() instanceof UUID) {
            projectUuid = (UUID) projectDto.getId();
        } else {
            try {
                // Try to convert to UUID from string representation
                projectUuid = UUID.fromString(projectDto.getId().toString());
            } catch (IllegalArgumentException e) {
                log.warn("Could not convert project ID to UUID: {}, using a random UUID instead", projectDto.getId());
                // If conversion fails, generate a random UUID
                projectUuid = UUID.randomUUID();
            }
        }
        
        project.setTestitId(projectUuid);
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setStatus(Project.Status.ACTIVE);
        Project savedProject = projectRepository.save(project);

        // Add project to user
        UserProject userProject = new UserProject();
        userProject.setUser(user);
        userProject.setProject(savedProject);
        userProject.setVisible(true);
        userProjectRepository.save(userProject);

        return savedProject;
    }

    /**
     * Update project visibility for user
     *
     * @param userId    User ID
     * @param projectId Project ID
     * @param visible   Visibility flag
     * @throws EntityNotFoundException if user-project relationship not found
     */
    @Transactional
    public void updateProjectVisibility(Long userId, Long projectId, boolean visible) {
        if (!userProjectRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new EntityNotFoundException("User-Project relationship not found");
        }
        userProjectRepository.updateVisibility(userId, projectId, visible);
    }

    /**
     * Remove project from user
     *
     * @param userId    User ID
     * @param projectId Project ID
     * @throws EntityNotFoundException if user-project relationship not found
     */
    @Transactional
    public void removeProjectFromUser(Long userId, Long projectId) {
        if (!userProjectRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new EntityNotFoundException("User-Project relationship not found");
        }
        userProjectRepository.deleteByUserIdAndProjectId(userId, projectId);
    }

    /**
     * Update project status
     *
     * @param id     Project ID
     * @param status New status
     * @return Updated project
     * @throws EntityNotFoundException if project not found
     */
    @Transactional
    public Project updateProjectStatus(Long id, Project.Status status) {
        Project project = getProjectById(id);
        project.setStatus(status);
        return projectRepository.save(project);
    }

    /**
     * Delete project
     *
     * @param id Project ID
     * @throws EntityNotFoundException if project not found
     */
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        
        // Сначала удаляем результаты тест-поинтов
        log.info("Deleting all test point results for project id: {}", id);
        testPointResultRepository.deleteAllByProjectId(id);
        
        // Затем удаляем статистику по тест-кейсам
        log.info("Deleting all test case statistics for project id: {}", id);
        testCaseStatisticsRepository.deleteAllByProjectId(id);
        
        // Затем удаляем статистику по запускам тестов
        log.info("Deleting all test run statistics for project id: {}", id);
        testRunStatisticsRepository.deleteAllByProjectId(id);
        
        // Затем удаляем все связи с пользователями
        log.info("Deleting all user-project relationships for project id: {}", id);
        userProjectRepository.deleteAllByProjectId(id);
        
        // Наконец удаляем сам проект
        log.info("Deleting project with id: {}", id);
        projectRepository.deleteById(id);
    }
}
