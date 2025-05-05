package com.testit.reports.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Statistics by user
    private List<UserStatistics> userStatistics;
    
    // Total statistics
    private int totalCreatedCount;
    private int totalModifiedCount;
    private int totalPassedCount;
    private int totalFailedCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatistics {
        private UUID userId;
        private String username;
        private int createdCount;
        private int modifiedCount;
        private int passedCount;
        private int failedCount;
        
        // Daily statistics
        private Map<LocalDate, DailyStatistics> dailyStatistics;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStatistics {
        private LocalDate date;
        private int createdCount;
        private int modifiedCount;
        private int passedCount;
        private int failedCount;
    }
}
