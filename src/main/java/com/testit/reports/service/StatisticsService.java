package com.testit.reports.service;

import com.testit.reports.client.testit.TestItApiClient;
import com.testit.reports.model.entity.Project;
import com.testit.reports.model.entity.TestCaseStatistics;
import com.testit.reports.model.entity.TestRunStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for collecting and retrieving statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TestItApiClient testItApiClient;
    private final ProjectService projectService;
    private final TestCaseStatisticsService testCaseStatisticsService;
    private final TestRunStatisticsService testRunStatisticsService;
    private final TestPointResultService testPointResultService;

    /**
     * Collect statistics for a project
     *
     * @param projectId Project ID
     * @param token     TestIT API token
     * @param startDate Start date
     * @param endDate   End date
     */
    @Transactional
    public void collectProjectStatistics(Long projectId, String token, LocalDate startDate, LocalDate endDate) {
        log.info("Starting to collect statistics for project ID: {}", projectId);
        
        Project project = projectService.getProjectById(projectId);
        log.info("Project details: ID={}, Name={}, TestIT ID={}, Status={}", 
                project.getId(), project.getName(), project.getTestitId(), project.getStatus());
        
        // Check if project is active
        if (project.getStatus() != Project.Status.ACTIVE) {
            log.warn("Project {} (ID: {}) is not active, skipping statistics collection", 
                    project.getName(), project.getId());
            return;
        }
        
        // Check if TestIT ID is valid
        if (project.getTestitId() == null) {
            log.error("Project {} (ID: {}) has null TestIT ID, skipping statistics collection", 
                    project.getName(), project.getId());
            return;
        }
        
        // Special handling for project with ID=3
        if (projectId == 3L) {
            log.info("Special handling for project with ID=3: {}, TestIT ID: {}", 
                    project.getName(), project.getTestitId());
            
            // Try to verify the project exists in TestIT
            try {
                log.info("Verifying project with TestIT ID: {} exists in TestIT", project.getTestitId());
                testItApiClient.getProject(token, project.getTestitId())
                    .doOnSuccess(p -> log.info("Project verified in TestIT: {}", p.getName()))
                    .doOnError(e -> log.error("Error verifying project in TestIT: {}", e.getMessage()))
                    .block();
            } catch (Exception e) {
                log.error("Exception while verifying project in TestIT: {}", e.getMessage(), e);
            }
        }
        
        // Collect work item statistics
        testCaseStatisticsService.collectWorkItemStatistics(project, token, startDate, endDate);
        
        // Collect test run statistics
        testRunStatisticsService.collectTestRunStatistics(project, token, startDate, endDate);
        
        log.info("Completed collecting statistics for project ID: {}", projectId);
    }

    /**
     * Get test case statistics for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of test case statistics
     */
    public List<TestCaseStatistics> getTestCaseStatistics(Long projectId, LocalDate startDate, LocalDate endDate) {
        return testCaseStatisticsService.getTestCaseStatistics(projectId, startDate, endDate);
    }

    /**
     * Get test run statistics for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of test run statistics
     */
    public List<TestRunStatistics> getTestRunStatistics(Long projectId, LocalDate startDate, LocalDate endDate) {
        return testRunStatisticsService.getTestRunStatistics(projectId, startDate, endDate);
    }

    /**
     * Get total created test cases count for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Total created test cases count
     */
    public int getTotalCreatedCount(Long projectId, LocalDate startDate, LocalDate endDate) {
        return testCaseStatisticsService.getTotalCreatedCount(projectId, startDate, endDate);
    }

    /**
     * Get total modified test cases count for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Total modified test cases count
     */
    public int getTotalModifiedCount(Long projectId, LocalDate startDate, LocalDate endDate) {
        return testCaseStatisticsService.getTotalModifiedCount(projectId, startDate, endDate);
    }

    /**
     * Get total passed tests count for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Total passed tests count
     */
    public int getTotalPassedCount(Long projectId, LocalDate startDate, LocalDate endDate) {
        return testRunStatisticsService.getTotalPassedCount(projectId, startDate, endDate);
    }

    /**
     * Get total failed tests count for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Total failed tests count
     */
    public int getTotalFailedCount(Long projectId, LocalDate startDate, LocalDate endDate) {
        return testRunStatisticsService.getTotalFailedCount(projectId, startDate, endDate);
    }
}
