package com.testit.reports.service;

import com.testit.reports.model.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ProjectService projectService;
    private final StatisticsService statisticsService;
    private final GlobalSettingService globalSettingService;

    /**
     * Scheduled task to collect statistics for all active projects
     * The cron expression is configured in application.properties
     */
    @Scheduled(cron = "${testit.api.scheduler.cron}")
    public void collectStatisticsForAllProjects() {
        log.info("Starting scheduled statistics collection for all active projects");
        
        try {
            // Get global token
            String token = globalSettingService.getGlobalTestItToken();
            if (token == null || token.isEmpty()) {
                log.error("Global TestIT token is not set. Skipping scheduled statistics collection.");
                return;
            }
            
            // Get all active projects
            List<Project> activeProjects = projectService.getAllActiveProjects();
            log.info("Found {} active projects", activeProjects.size());
            
            // Calculate date range (yesterday)
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            // Collect statistics for each project
            for (Project project : activeProjects) {
                try {
                    log.info("Collecting statistics for project: {} (ID: {})", project.getName(), project.getId());
                    statisticsService.collectProjectStatistics(project.getId(), token, yesterday, yesterday);
                    log.info("Statistics collection completed for project: {}", project.getName());
                } catch (Exception e) {
                    log.error("Error collecting statistics for project: {} (ID: {})", project.getName(), project.getId(), e);
                }
            }
            
            log.info("Scheduled statistics collection completed for all active projects");
        } catch (Exception e) {
            log.error("Error in scheduled statistics collection", e);
        }
    }

    /**
     * Manually collect statistics for all active projects for a date range
     *
     * @param startDate Start date
     * @param endDate   End date
     */
    public void manuallyCollectStatisticsForAllProjects(LocalDate startDate, LocalDate endDate) {
        log.info("Starting manual statistics collection for all active projects from {} to {}", startDate, endDate);
        
        try {
            // Get global token
            String token = globalSettingService.getGlobalTestItToken();
            if (token == null || token.isEmpty()) {
                log.error("Global TestIT token is not set. Skipping manual statistics collection.");
                return;
            }
            
            // Get all active projects
            List<Project> activeProjects = projectService.getAllActiveProjects();
            log.info("Found {} active projects", activeProjects.size());
            
            // Collect statistics for each project
            for (Project project : activeProjects) {
                try {
                    log.info("Collecting statistics for project: {} (ID: {})", project.getName(), project.getId());
                    statisticsService.collectProjectStatistics(project.getId(), token, startDate, endDate);
                    log.info("Statistics collection completed for project: {}", project.getName());
                } catch (Exception e) {
                    log.error("Error collecting statistics for project: {} (ID: {})", project.getName(), project.getId(), e);
                }
            }
            
            log.info("Manual statistics collection completed for all active projects");
        } catch (Exception e) {
            log.error("Error in manual statistics collection", e);
        }
    }

    /**
     * Manually collect statistics for a specific project for a date range
     *
     * @param projectId Project ID
     * @param token     TestIT API token
     * @param startDate Start date
     * @param endDate   End date
     */
    public void manuallyCollectStatisticsForProject(Long projectId, String token, LocalDate startDate, LocalDate endDate) {
        log.info("Starting manual statistics collection for project ID: {} from {} to {}", projectId, startDate, endDate);
        log.info("Token received: {}", token);
        
        try {
            // Use global token if not provided
            if (token == null || token.isEmpty()) {
                token = globalSettingService.getGlobalTestItToken();
                log.info("Using global token: {}", token);
                
                if (token == null || token.isEmpty()) {
                    log.error("TestIT token is not provided and global token is not set. Skipping manual statistics collection.");
                    return;
                }
            }
            
            // Collect statistics
            log.info("Calling statisticsService.collectProjectStatistics with token: {}", token);
            statisticsService.collectProjectStatistics(projectId, token, startDate, endDate);
            log.info("Manual statistics collection completed for project ID: {}", projectId);
        } catch (Exception e) {
            log.error("Error in manual statistics collection for project ID: {}", projectId, e);
        }
    }
}
