package com.testit.reports.controller;

import com.testit.reports.controller.dto.ApiResponse;
import com.testit.reports.controller.dto.StatisticsDto;
import com.testit.reports.model.entity.Project;
import com.testit.reports.model.entity.TestCaseStatistics;
import com.testit.reports.model.entity.TestRunStatistics;
import com.testit.reports.model.entity.User;
import com.testit.reports.service.ProjectService;
import com.testit.reports.service.SchedulerService;
import com.testit.reports.service.StatisticsService;
import com.testit.reports.service.TestPointResultService;
import com.testit.reports.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ProjectService projectService;
    private final UserService userService;
    private final SchedulerService schedulerService;
    private final TestPointResultService testPointResultService;

    /**
     * Get statistics for a project
     *
     * @param projectId   Project ID
     * @param startDate   Start date
     * @param endDate     End date
     * @param userDetails Current user details
     * @return Statistics
     */
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<StatisticsDto>> getProjectStatistics(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Project project = projectService.getProjectById(projectId);

            // Check if user has access to project
            boolean hasAccess = user.getRole() == User.Role.ADMIN ||
                    projectService.getAllProjectsByUserId(user.getId()).stream()
                            .anyMatch(p -> p.getId().equals(projectId));

            if (!hasAccess) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }

            // Get statistics
            List<TestCaseStatistics> testCaseStatistics = statisticsService.getTestCaseStatistics(projectId, startDate, endDate);
            List<TestRunStatistics> testRunStatistics = statisticsService.getTestRunStatistics(projectId, startDate, endDate);

            // Calculate total statistics
            int totalCreatedCount = statisticsService.getTotalCreatedCount(projectId, startDate, endDate);
            int totalModifiedCount = statisticsService.getTotalModifiedCount(projectId, startDate, endDate);
            
            // Use TestPointResultService to get accurate counts directly from test_point_results table
            int totalPassedCount = testPointResultService.countPassedTestPoints(projectId, startDate, endDate);
            int totalFailedCount = testPointResultService.countFailedTestPoints(projectId, startDate, endDate);
            
            log.info("Project {}: Total passed: {}, failed: {} for period {} to {}", 
                    projectId, totalPassedCount, totalFailedCount, startDate, endDate);

            // Group statistics by user
            Map<UUID, StatisticsDto.UserStatistics> userStatisticsMap = new HashMap<>();

            // Process test case statistics
            for (TestCaseStatistics tcs : testCaseStatistics) {
                UUID userId = tcs.getTestitUserId();
                StatisticsDto.UserStatistics userStats = userStatisticsMap.computeIfAbsent(userId,
                        id -> StatisticsDto.UserStatistics.builder()
                                .userId(id)
                                .username(tcs.getTestitUsername())
                                .createdCount(0)
                                .modifiedCount(0)
                                .passedCount(0)
                                .failedCount(0)
                                .dailyStatistics(new HashMap<>())
                                .build());

                userStats.setCreatedCount(userStats.getCreatedCount() + tcs.getCreatedCount());
                userStats.setModifiedCount(userStats.getModifiedCount() + tcs.getModifiedCount());

                // Add daily statistics
                StatisticsDto.DailyStatistics dailyStats = userStats.getDailyStatistics()
                        .computeIfAbsent(tcs.getDate(), date -> StatisticsDto.DailyStatistics.builder()
                                .date(date)
                                .createdCount(0)
                                .modifiedCount(0)
                                .passedCount(0)
                                .failedCount(0)
                                .build());

                dailyStats.setCreatedCount(tcs.getCreatedCount());
                dailyStats.setModifiedCount(tcs.getModifiedCount());
            }

            // Process test run statistics - use test_point_results for more accurate data
            for (TestRunStatistics trs : testRunStatistics) {
                UUID userId = trs.getTestitUserId();
                LocalDate date = trs.getDate();
                
                // Get user statistics from map or create new
                StatisticsDto.UserStatistics userStats = userStatisticsMap.computeIfAbsent(userId,
                        id -> StatisticsDto.UserStatistics.builder()
                                .userId(id)
                                .username(trs.getTestitUsername())
                                .createdCount(0)
                                .modifiedCount(0)
                                .passedCount(0)
                                .failedCount(0)
                                .dailyStatistics(new HashMap<>())
                                .build());
                
                // Calculate passed and failed counts directly from test_point_results
                int passedCount = testPointResultService.countPassedTestPointsForUser(projectId, userId, date, date);
                int failedCount = testPointResultService.countFailedTestPointsForUser(projectId, userId, date, date);
                
                log.info("User {} on date {}: passed={}, failed={}", userId, date, passedCount, failedCount);
                
                // Set user statistics
                userStats.setPassedCount(userStats.getPassedCount() + passedCount);
                userStats.setFailedCount(userStats.getFailedCount() + failedCount);
                
                // Add daily statistics
                StatisticsDto.DailyStatistics dailyStats = userStats.getDailyStatistics()
                        .computeIfAbsent(date, d -> StatisticsDto.DailyStatistics.builder()
                                .date(d)
                                .createdCount(0)
                                .modifiedCount(0)
                                .passedCount(0)
                                .failedCount(0)
                                .build());
                
                dailyStats.setPassedCount(passedCount);
                dailyStats.setFailedCount(failedCount);
            }

            // Build response
            StatisticsDto statisticsDto = StatisticsDto.builder()
                    .projectId(projectId)
                    .projectName(project.getName())
                    .startDate(startDate)
                    .endDate(endDate)
                    .userStatistics(new ArrayList<>(userStatisticsMap.values()))
                    .totalCreatedCount(totalCreatedCount)
                    .totalModifiedCount(totalModifiedCount)
                    .totalPassedCount(totalPassedCount)
                    .totalFailedCount(totalFailedCount)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(statisticsDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting project statistics", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Manually collect statistics for a project
     *
     * @param projectId   Project ID
     * @param startDate   Start date
     * @param endDate     End date
     * @param userDetails Current user details
     * @return Success response
     */
    @PostMapping("/projects/{projectId}/collect")
    public ResponseEntity<ApiResponse<Void>> collectProjectStatistics(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Manually collecting statistics for project ID: {}, startDate: {}, endDate: {}", 
                    projectId, startDate, endDate);
            
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            log.info("User found: {}", user.getUsername());

            // Check if project exists
            Project project = projectService.getProjectById(projectId);
            log.info("Project found: {}, TestIT ID: {}", project.getName(), project.getTestitId());

            // Check if user has access to project
            boolean hasAccess = user.getRole() == User.Role.ADMIN ||
                    projectService.getAllProjectsByUserId(user.getId()).stream()
                            .anyMatch(p -> p.getId().equals(projectId));

            if (!hasAccess) {
                log.warn("User {} does not have access to project {}", user.getUsername(), projectId);
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }

            // Get user's TestIT token
            String token = user.getTestitToken();
            log.info("User's TestIT token: {}", token);
            
            if (token == null || token.isEmpty()) {
                log.warn("TestIT token is not set for user {}", user.getUsername());
                return ResponseEntity.badRequest().body(ApiResponse.error("TestIT token is not set for the user"));
            }

            // Collect statistics
            log.info("Starting statistics collection for project ID: {}", projectId);
            schedulerService.manuallyCollectStatisticsForProject(projectId, token, startDate, endDate);
            log.info("Statistics collection started for project ID: {}", projectId);

            return ResponseEntity.ok(ApiResponse.success("Statistics collection started"));
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error collecting project statistics", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Manually collect statistics for all projects
     *
     * @param startDate   Start date
     * @param endDate     End date
     * @param userDetails Current user details
     * @return Success response
     */
    @PostMapping("/collect-all")
    public ResponseEntity<ApiResponse<Void>> collectAllProjectsStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Only admin can collect statistics for all projects
            if (user.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }

            // Collect statistics
            schedulerService.manuallyCollectStatisticsForAllProjects(startDate, endDate);

            return ResponseEntity.ok(ApiResponse.success("Statistics collection started for all projects"));
        } catch (Exception e) {
            log.error("Error collecting statistics for all projects", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
