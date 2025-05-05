package com.testit.reports.service;

import com.testit.reports.client.testit.TestItApiClient;
import com.testit.reports.client.testit.dto.ProjectDto;
import com.testit.reports.client.testit.dto.TestPlanDto;
import com.testit.reports.client.testit.dto.TestPointDto;
import com.testit.reports.client.testit.dto.TestPointSearchRequest;
import com.testit.reports.model.entity.Project;
import com.testit.reports.model.entity.TestRunStatistics;
import com.testit.reports.repository.TestRunStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing test run statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunStatisticsService {

    private final TestItApiClient testItApiClient;
    private final TestRunStatisticsRepository testRunStatisticsRepository;
    private final TestPointResultService testPointResultService;

    /**
     * Collect test run statistics for a project
     *
     * @param project   Project
     * @param token     TestIT API token
     * @param startDate Start date
     * @param endDate   End date
     */
    @Transactional
    public void collectTestRunStatistics(Project project, String token, LocalDate startDate, LocalDate endDate) {
        log.info("Collecting test run statistics for project: {} (ID: {}, TestIT ID: {}), token: {}, startDate: {}, endDate: {}", 
                project.getName(), project.getId(), project.getTestitId(), token, startDate, endDate);
        
        try {
            // Special handling for project with ID=3
            if (project.getId() == 3L) {
                log.info("Special handling for project with ID=3 in collectTestRunStatistics");
                log.info("Project details: ID={}, Name={}, TestIT ID={}, Status={}", 
                        project.getId(), project.getName(), project.getTestitId(), project.getStatus());
                
                // Try to verify the project exists in TestIT
                try {
                    log.info("Verifying project with TestIT ID: {} exists in TestIT", project.getTestitId());
                    ProjectDto projectDto = testItApiClient.getProject(token, project.getTestitId())
                        .doOnSuccess(p -> log.info("Project verified in TestIT: {}", p.getName()))
                        .doOnError(e -> log.error("Error verifying project in TestIT: {}", e.getMessage()))
                        .block();
                    
                    if (projectDto != null) {
                        log.info("Project found in TestIT: {}, ID: {}", projectDto.getName(), projectDto.getId());
                    } else {
                        log.error("Project not found in TestIT: {}", project.getTestitId());
                    }
                } catch (Exception e) {
                    log.error("Exception while verifying project in TestIT: {}", e.getMessage(), e);
                }
            }
            
            // Get test plans for the project
            log.info("Getting test plans for project: {} (ID: {}, TestIT ID: {})", 
                    project.getName(), project.getId(), project.getTestitId());
            List<TestPlanDto> testPlans = testItApiClient.getProjectTestPlans(token, project.getTestitId());
            log.info("Received {} test plans for project: {} (ID: {})", 
                    testPlans != null ? testPlans.size() : 0, project.getName(), project.getId());
            
            if (testPlans == null || testPlans.isEmpty()) {
                log.info("No test plans found for project: {} (ID: {})", project.getName(), project.getId());
                return;
            }
            
            // Log all test plans
            for (TestPlanDto testPlan : testPlans) {
                log.info("Test plan: {} (ID: {}), Status: {}, Created: {}, Completed: {}", 
                        testPlan.getName(), testPlan.getId(), testPlan.getStatus(), 
                        testPlan.getCreatedDate(), testPlan.getCompletedOn());
            }
            
            // Filter test plans by date range or include all if project ID is 3
            List<TestPlanDto> filteredTestPlans = filterTestPlansByDateRange(testPlans, project, startDate, endDate);
            
            log.info("Filtered to {} test plans for project: {} (ID: {})", 
                    filteredTestPlans.size(), project.getName(), project.getId());
            
            // Process each test plan
            for (TestPlanDto testPlan : filteredTestPlans) {
                processTestPlan(project, token, testPlan, startDate, endDate);
            }
        } catch (Exception e) {
            log.error("Error collecting test run statistics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Filter test plans by date range
     *
     * @param testPlans  Test plans
     * @param project    Project
     * @param startDate  Start date
     * @param endDate    End date
     * @return Filtered test plans
     */
    private List<TestPlanDto> filterTestPlansByDateRange(List<TestPlanDto> testPlans, Project project, LocalDate startDate, LocalDate endDate) {
        return testPlans.stream()
                .filter(tp -> {
                    // For project with ID=3, include all test plans regardless of completion date
                    if (project.getId() == 3L) {
                        log.info("Including all test plans for project with ID=3, including test plan: {} ({})", 
                                tp.getName(), tp.getId());
                        return true;
                    }
                    
                    // For other projects, check both creation and completion dates
                    boolean inRange = false;
                    
                    // Check completion date if available
                    if (tp.getCompletedOn() != null) {
                        LocalDate completionDate = tp.getCompletedOn().toLocalDate();
                        boolean completionInRange = !completionDate.isBefore(startDate) && !completionDate.isAfter(endDate);
                        log.info("Test plan {} has completion date: {}, in range: {}", tp.getId(), completionDate, completionInRange);
                        if (completionInRange) {
                            inRange = true;
                        }
                    }
                    
                    // Check creation date if available
                    if (!inRange && tp.getCreatedDate() != null) {
                        LocalDate creationDate = tp.getCreatedDate().toLocalDate();
                        boolean creationInRange = !creationDate.isBefore(startDate) && !creationDate.isAfter(endDate);
                        log.info("Test plan {} has creation date: {}, in range: {}", tp.getId(), creationDate, creationInRange);
                        if (creationInRange) {
                            inRange = true;
                        }
                    }
                    
                    // If neither date is in range or available, use current date
                    if (!inRange && tp.getCompletedOn() == null && tp.getCreatedDate() == null) {
                        log.info("Test plan {} has neither completion nor creation date, using current date", tp.getId());
                        inRange = true; // Include it by default
                    }
                    
                    if (!inRange) {
                        log.info("Test plan {} is outside date range {}-{}, skipping", tp.getId(), startDate, endDate);
                    } else {
                        log.info("Test plan {} is within date range {}-{}, including", tp.getId(), startDate, endDate);
                    }
                    
                    return inRange;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Process a test plan
     *
     * @param project    Project
     * @param token      TestIT API token
     * @param testPlan   Test plan
     * @param startDate  Start date
     * @param endDate    End date
     */
    private void processTestPlan(Project project, String token, TestPlanDto testPlan, LocalDate startDate, LocalDate endDate) {
        log.info("Processing test plan: {} ({})", testPlan.getName(), testPlan.getId());
        
        // Get test points for the test plan
        TestPointSearchRequest request = TestPointSearchRequest.builder()
                .testPlanIds(Collections.singletonList(testPlan.getId()))
                .workItemIsDeleted(false)
                .build();
        
        List<TestPointDto> testPoints = testItApiClient.searchTestPoints(token, request, 0, 1000);
        log.info("Received {} test points for test plan", testPoints != null ? testPoints.size() : 0);
        
        if (testPoints == null || testPoints.isEmpty()) {
            log.info("No test points found for test plan: {}", testPlan.getName());
            return;
        }
        
        // Determine date to use for statistics
        LocalDate testPlanDate = determineTestPlanDate(testPlan);
        
        // Group test points by user and status
        Map<UUID, Map<String, Integer>> pointsByUserAndStatus = groupTestPointsByUserAndStatus(project, testPoints, testPlan, testPlanDate);
        
        // Process each user's test points
        for (Map.Entry<UUID, Map<String, Integer>> entry : pointsByUserAndStatus.entrySet()) {
            UUID userId = entry.getKey();
            Map<String, Integer> statusCounts = entry.getValue();
            
            processUserTestPoints(project, userId, statusCounts, testPlanDate);
        }
    }
    
    /**
     * Determine date to use for statistics
     *
     * @param testPlan Test plan
     * @return Date to use
     */
    private LocalDate determineTestPlanDate(TestPlanDto testPlan) {
        LocalDate testPlanDate;
        if (testPlan.getCompletedOn() != null) {
            testPlanDate = testPlan.getCompletedOn().toLocalDate();
            log.info("Using completion date for statistics: {}", testPlanDate);
        } else if (testPlan.getCreatedDate() != null) {
            testPlanDate = testPlan.getCreatedDate().toLocalDate();
            log.info("Test plan has no completion date, using creation date for statistics: {}", testPlanDate);
        } else {
            testPlanDate = LocalDate.now();
            log.info("Test plan has neither completion nor creation date, using current date for statistics: {}", testPlanDate);
        }
        return testPlanDate;
    }
    
    /**
     * Group test points by user and status
     *
     * @param project    Project
     * @param testPoints Test points
     * @param testPlan   Test plan
     * @param testPlanDate Date to use for statistics
     * @return Map of user ID to map of status to count
     */
    private Map<UUID, Map<String, Integer>> groupTestPointsByUserAndStatus(Project project, List<TestPointDto> testPoints, TestPlanDto testPlan, LocalDate testPlanDate) {
        Map<UUID, Map<String, Integer>> pointsByUserAndStatus = new HashMap<>();
        
        for (TestPointDto testPoint : testPoints) {
            // Use modifiedById as the user who executed the test
            UUID userId = testPoint.getModifiedById();
            if (userId == null) {
                log.info("Test point {} has no modifiedById, using createdById", testPoint.getId());
                userId = testPoint.getCreatedById();
            }
            
            if (userId == null) {
                log.info("Test point {} has no user ID, skipping", testPoint.getId());
                continue;
            }
            
            // Get or create user map
            Map<String, Integer> statusCounts = pointsByUserAndStatus.computeIfAbsent(userId, id -> new HashMap<>());
            
            // Increment count for status
            String status = testPoint.getStatus();
            
            // Log status for debugging
            if (project.getId() == 3L) {
                log.info("Test point {} has status: {}, statusModel: {}", 
                        testPoint.getId(), status, testPoint.getStatusModel());
            }
            
            // Check if status is null
            if (status == null && testPoint.getStatusModel() != null) {
                status = testPoint.getStatusModel().getCode();
                log.info("Using status code from statusModel: {}", status);
            }
            
            // Default to "Unknown" if still null
            if (status == null) {
                status = "Unknown";
                log.warn("Test point {} has null status, using 'Unknown'", testPoint.getId());
            }
            
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            
            // Save individual test point result to the new table
            testPointResultService.saveTestPointResult(project, testPlan.getId(), testPoint, userId, status, testPlanDate);
        }
        
        return pointsByUserAndStatus;
    }
    
    /**
     * Process a user's test points
     *
     * @param project      Project
     * @param userId       User ID
     * @param statusCounts Map of status to count
     * @param testPlanDate Date to use for statistics
     */
    private void processUserTestPoints(Project project, UUID userId, Map<String, Integer> statusCounts, LocalDate testPlanDate) {
        // Log status counts for debugging
        if (project.getId() == 3L) {
            log.info("User {} status counts: {}", userId, statusCounts);
        }
        
        // Instead of adding up counts from the current test plan, we'll calculate them from test_point_results
        int passedCount = testPointResultService.countPassedTestPointsForUser(project.getId(), userId, testPlanDate, testPlanDate);
        int failedCount = testPointResultService.countFailedTestPointsForUser(project.getId(), userId, testPlanDate, testPlanDate);
        
        log.info("Calculated from test_point_results: passedCount: {}, failedCount: {}", passedCount, failedCount);
        
        // Skip if no passed or failed tests
        if (passedCount == 0 && failedCount == 0) {
            log.info("User {} has no passed or failed tests, skipping", userId);
            return;
        }
        
        // Find or create statistics record
        TestRunStatistics statistics = testRunStatisticsRepository
                .findByProjectIdAndTestitUserIdAndDate(project.getId(), userId, testPlanDate)
                .orElse(new TestRunStatistics());
        
        // Set or update statistics
        statistics.setProject(project);
        statistics.setTestitUserId(userId);
        
        // Find username from test points
        String username = "User " + userId.toString().substring(0, 8);
        statistics.setTestitUsername(username);
        
        statistics.setDate(testPlanDate);
        
        // Always set the counts directly from test_point_results
        statistics.setPassedCount(passedCount);
        statistics.setFailedCount(failedCount);
        
        log.info("Saving test run statistics for user: {}, date: {}, passed: {}, failed: {}", 
                userId, testPlanDate, passedCount, failedCount);
        
        testRunStatisticsRepository.save(statistics);
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
        return testRunStatisticsRepository.findStatisticsByProjectAndDateRange(projectId, startDate, endDate);
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
        Integer count = testRunStatisticsRepository.getTotalPassedCountByProjectAndDateRange(projectId, startDate, endDate);
        return count != null ? count : 0;
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
        Integer count = testRunStatisticsRepository.getTotalFailedCountByProjectAndDateRange(projectId, startDate, endDate);
        return count != null ? count : 0;
    }
}
