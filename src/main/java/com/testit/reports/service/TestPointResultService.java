package com.testit.reports.service;

import com.testit.reports.client.testit.TestItApiClient;
import com.testit.reports.client.testit.dto.TestPointDto;
import com.testit.reports.model.entity.Project;
import com.testit.reports.model.entity.TestPointResult;
import com.testit.reports.repository.TestPointResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing test point results
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestPointResultService {

    private final TestPointResultRepository testPointResultRepository;
    private final TestItApiClient testItApiClient;

    /**
     * Save test point result to the database
     *
     * @param project    Project
     * @param testPlanId Test plan ID
     * @param testPoint  Test point
     * @param userId     User ID
     * @param status     Status
     * @param date       Date
     * @param token      TestIT API token (can be null)
     * @return Saved test point result or null if error
     */
    @Transactional
    public TestPointResult saveTestPointResult(Project project, UUID testPlanId, TestPointDto testPoint, UUID userId, String status, LocalDate date, String token) {
        try {
            TestPointResult result;
            
            // Check if test point already exists
            Optional<TestPointResult> existingResult = testPointResultRepository.findByTestPointId(testPoint.getId());
            if (existingResult.isPresent()) {
                // Update existing test point result
                result = existingResult.get();
                log.info("Test point {} already exists in the database, updating status from {} to {}", 
                        testPoint.getId(), result.getStatus(), status);
                
                // Update status and date
                result.setStatus(status);
                result.setDate(date);
            } else {
                // Create new test point result
                result = new TestPointResult();
                result.setProject(project);
                result.setTestPlanId(testPlanId);
                result.setTestPointId(testPoint.getId());
                result.setTestitUserId(userId);
                
                // Get real username from TestIT API
                String username;
                try {
                    username = testItApiClient.getUserName(token, userId);
                    log.info("Got real username from TestIT API: {}", username);
                } catch (Exception e) {
                    // Fallback to default name if API call fails
                    username = "User " + userId.toString().substring(0, 8);
                    log.warn("Failed to get real username from TestIT API, using fallback: {}", username);
                }
                result.setTestitUsername(username);
                
                result.setStatus(status);
                result.setDate(date);
                
                log.info("Creating new test point result: {}, status: {}, user: {}, date: {}", 
                        testPoint.getId(), status, userId, date);
            }
            
            // Save to database
            TestPointResult savedResult = testPointResultRepository.save(result);
            log.info("Saved test point result: {}, status: {}, user: {}, date: {}", 
                    testPoint.getId(), status, userId, date);
            
            return savedResult;
        } catch (Exception e) {
            log.error("Error saving test point result: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get test point results for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of test point results
     */
    public List<TestPointResult> getTestPointResults(Long projectId, LocalDate startDate, LocalDate endDate) {
        return testPointResultRepository.findByProjectIdAndDateBetween(projectId, startDate, endDate);
    }
    
    /**
     * Get test point results for a user in a project in a date range
     *
     * @param projectId Project ID
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of test point results
     */
    public List<TestPointResult> getTestPointResultsForUser(Long projectId, UUID userId, LocalDate startDate, LocalDate endDate) {
        return testPointResultRepository.findByProjectIdAndUserIdAndDateBetween(projectId, userId, startDate, endDate);
    }
    
    /**
     * Count passed test points for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Count of passed test points
     */
    public int countPassedTestPoints(Long projectId, LocalDate startDate, LocalDate endDate) {
        Integer count = testPointResultRepository.countPassedByProjectIdAndDateBetween(projectId, startDate, endDate);
        return count != null ? count : 0;
    }
    
    /**
     * Count failed test points for a project in a date range
     *
     * @param projectId Project ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Count of failed test points
     */
    public int countFailedTestPoints(Long projectId, LocalDate startDate, LocalDate endDate) {
        Integer count = testPointResultRepository.countFailedByProjectIdAndDateBetween(projectId, startDate, endDate);
        return count != null ? count : 0;
    }
    
    /**
     * Count passed test points for a user in a project in a date range
     *
     * @param projectId Project ID
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Count of passed test points
     */
    public int countPassedTestPointsForUser(Long projectId, UUID userId, LocalDate startDate, LocalDate endDate) {
        Integer count = testPointResultRepository.countPassedByProjectIdAndUserIdAndDateBetween(projectId, userId, startDate, endDate);
        return count != null ? count : 0;
    }
    
    /**
     * Count failed test points for a user in a project in a date range
     *
     * @param projectId Project ID
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return Count of failed test points
     */
    public int countFailedTestPointsForUser(Long projectId, UUID userId, LocalDate startDate, LocalDate endDate) {
        Integer count = testPointResultRepository.countFailedByProjectIdAndUserIdAndDateBetween(projectId, userId, startDate, endDate);
        return count != null ? count : 0;
    }
    
    /**
     * Update usernames for all test point results
     *
     * @param token TestIT API token
     */
    @Transactional
    public void updateAllUsernames(String token) {
        log.info("Updating usernames for all test point results");
        
        // Get all test point results
        List<TestPointResult> allResults = testPointResultRepository.findAll();
        log.info("Found {} test point results", allResults.size());
        
        // Update username for each record
        for (TestPointResult result : allResults) {
            UUID userId = result.getTestitUserId();
            if (userId != null) {
                try {
                    String username = testItApiClient.getUserName(token, userId);
                    log.info("Updating username for user {}: {} -> {}", 
                            userId, result.getTestitUsername(), username);
                    result.setTestitUsername(username);
                    testPointResultRepository.save(result);
                } catch (Exception e) {
                    log.error("Error updating username for user {}: {}", userId, e.getMessage(), e);
                }
            }
        }
        
        log.info("Username update completed for test point results");
    }
}
