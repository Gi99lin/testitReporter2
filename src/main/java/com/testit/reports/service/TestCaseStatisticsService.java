package com.testit.reports.service;

import com.testit.reports.client.testit.TestItApiClient;
import com.testit.reports.client.testit.dto.DateRangeFilter;
import com.testit.reports.client.testit.dto.WorkItemDto;
import com.testit.reports.client.testit.dto.WorkItemSearchRequest;
import com.testit.reports.model.entity.Project;
import com.testit.reports.model.entity.TestCaseStatistics;
import com.testit.reports.repository.TestCaseStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing test case statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseStatisticsService {

    private final TestItApiClient testItApiClient;
    private final TestCaseStatisticsRepository testCaseStatisticsRepository;

    /**
     * Collect work item (test case) statistics for a project
     *
     * @param project   Project
     * @param token     TestIT API token
     * @param startDate Start date
     * @param endDate   End date
     */
    @Transactional
    public void collectWorkItemStatistics(Project project, String token, LocalDate startDate, LocalDate endDate) {
        log.info("Collecting work item statistics for project: {}, token: {}, startDate: {}, endDate: {}", 
                project.getName(), token, startDate, endDate);
        
        // Prepare date range for API request
        OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1).atOffset(ZoneOffset.UTC);
        log.info("Date range: from {} to {}", startDateTime, endDateTime);

        // Collect created test cases
        WorkItemSearchRequest createdRequest = WorkItemSearchRequest.builder()
                .filter(WorkItemSearchRequest.WorkItemFilter.builder()
                        .projectIds(Collections.singletonList(project.getTestitId()))
                        .types(Collections.singletonList("TestCases"))
                        .createdDate(DateRangeFilter.builder()
                                .from(startDateTime)
                                .to(endDateTime)
                                .build())
                        .build())
                .build();
        log.info("Created test cases request: {}", createdRequest);

        try {
            log.info("Calling testItApiClient.searchWorkItems for created test cases with token: {}", token);
            List<WorkItemDto> createdWorkItems = testItApiClient.searchWorkItems(token, createdRequest);
            log.info("Received {} created work items", createdWorkItems != null ? createdWorkItems.size() : 0);
            
            // Collect modified test cases
            WorkItemSearchRequest modifiedRequest = WorkItemSearchRequest.builder()
                    .filter(WorkItemSearchRequest.WorkItemFilter.builder()
                            .projectIds(Collections.singletonList(project.getTestitId()))
                            .types(Collections.singletonList("TestCases"))
                            .modifiedDate(DateRangeFilter.builder()
                                    .from(startDateTime)
                                    .to(endDateTime)
                                    .build())
                            .build())
                    .build();
            log.info("Modified test cases request: {}", modifiedRequest);

            log.info("Calling testItApiClient.searchWorkItems for modified test cases with token: {}", token);
            List<WorkItemDto> modifiedWorkItems = testItApiClient.searchWorkItems(token, modifiedRequest);
            log.info("Received {} modified work items", modifiedWorkItems != null ? modifiedWorkItems.size() : 0);

            // Group by user and date
            Map<UUID, Map<LocalDate, Integer>> createdCountByUserAndDate = groupWorkItemsByUserAndDate(createdWorkItems, true);
            Map<UUID, Map<LocalDate, Integer>> modifiedCountByUserAndDate = groupWorkItemsByUserAndDate(modifiedWorkItems, false);

            // Merge and save statistics
            Set<UUID> allUserIds = new HashSet<>();
            allUserIds.addAll(createdCountByUserAndDate.keySet());
            allUserIds.addAll(modifiedCountByUserAndDate.keySet());

            for (UUID userId : allUserIds) {
                Map<LocalDate, Integer> createdByDate = createdCountByUserAndDate.getOrDefault(userId, Collections.emptyMap());
                Map<LocalDate, Integer> modifiedByDate = modifiedCountByUserAndDate.getOrDefault(userId, Collections.emptyMap());

                Set<LocalDate> allDates = new HashSet<>();
                allDates.addAll(createdByDate.keySet());
                allDates.addAll(modifiedByDate.keySet());

                for (LocalDate date : allDates) {
                    int createdCount = createdByDate.getOrDefault(date, 0);
                    int modifiedCount = modifiedByDate.getOrDefault(date, 0);

                    // Find or create statistics record
                    TestCaseStatistics statistics = testCaseStatisticsRepository
                            .findByProjectIdAndTestitUserIdAndDate(project.getId(), userId, date)
                            .orElse(new TestCaseStatistics());

                    // Set or update statistics
                    statistics.setProject(project);
                    statistics.setTestitUserId(userId);
                    
                    // Find username from work items
                    String username = findUsernameForUser(userId, createdWorkItems, modifiedWorkItems);
                    statistics.setTestitUsername(username != null ? username : "Unknown");
                    
                    statistics.setDate(date);
                    
                    // Check if this is a new record or an existing one
                    boolean isNewRecord = statistics.getId() == null;
                    
                    if (isNewRecord) {
                        // For new records, set the counts directly
                        log.info("Creating new work item statistics record for user: {}, date: {}, created: {}, modified: {}", 
                                userId, date, createdCount, modifiedCount);
                    } else {
                        // For existing records, replace the counts
                        log.info("Updating existing work item statistics record for user: {}, date: {}, old created: {}, old modified: {}, new created: {}, new modified: {}", 
                                userId, date, statistics.getCreatedCount(), statistics.getModifiedCount(), createdCount, modifiedCount);
                    }
                    
                    // Always set the counts (whether new or existing record)
                    statistics.setCreatedCount(createdCount);
                    statistics.setModifiedCount(modifiedCount);

                    testCaseStatisticsRepository.save(statistics);
                }
            }
        } catch (Exception e) {
            log.error("Error collecting work item statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * Group work items by user and date
     *
     * @param workItems   Work items
     * @param isCreated   Whether to use created date (true) or modified date (false)
     * @return Map of user ID to map of date to count
     */
    private Map<UUID, Map<LocalDate, Integer>> groupWorkItemsByUserAndDate(List<WorkItemDto> workItems, boolean isCreated) {
        return workItems.stream()
                .collect(Collectors.groupingBy(
                        item -> isCreated ? item.getCreatedById() : item.getModifiedById(),
                        Collectors.groupingBy(
                                item -> (isCreated ? item.getCreatedDate() : item.getModifiedDate()).toLocalDate(),
                                Collectors.summingInt(item -> 1)
                        )
                ));
    }

    /**
     * Find username for a user from work items
     *
     * @param userId          User ID
     * @param createdWorkItems  Created work items
     * @param modifiedWorkItems Modified work items
     * @return Username or null if not found
     */
    private String findUsernameForUser(UUID userId, List<WorkItemDto> createdWorkItems, List<WorkItemDto> modifiedWorkItems) {
        // Try to find in created work items
        Optional<WorkItemDto> createdItem = createdWorkItems.stream()
                .filter(item -> userId.equals(item.getCreatedById()))
                .findFirst();
        
        if (createdItem.isPresent()) {
            // We don't have username in the DTO, so we'll use the ID as a placeholder
            return "User " + userId.toString().substring(0, 8);
        }
        
        // Try to find in modified work items
        Optional<WorkItemDto> modifiedItem = modifiedWorkItems.stream()
                .filter(item -> userId.equals(item.getModifiedById()))
                .findFirst();
        
        if (modifiedItem.isPresent()) {
            // We don't have username in the DTO, so we'll use the ID as a placeholder
            return "User " + userId.toString().substring(0, 8);
        }
        
        return null;
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
        return testCaseStatisticsRepository.findStatisticsByProjectAndDateRange(projectId, startDate, endDate);
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
        Integer count = testCaseStatisticsRepository.getTotalCreatedCountByProjectAndDateRange(projectId, startDate, endDate);
        return count != null ? count : 0;
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
        Integer count = testCaseStatisticsRepository.getTotalModifiedCountByProjectAndDateRange(projectId, startDate, endDate);
        return count != null ? count : 0;
    }
}
