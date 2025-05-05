package com.testit.reports.client.testit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRunDto {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("launchSource")
    private String launchSource;
    
    @JsonProperty("startedOn")
    private LocalDateTime startedOn;
    
    @JsonProperty("completedOn")
    private LocalDateTime completedOn;
    
    @JsonProperty("status")
    private TestRunStatus status;
    
    @JsonProperty("projectId")
    private UUID projectId;
    
    @JsonProperty("testPlanId")
    private UUID testPlanId;
    
    @JsonProperty("testResults")
    private List<TestResultDto> testResults;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("modifiedDate")
    private LocalDateTime modifiedDate;
    
    @JsonProperty("createdById")
    private UUID createdById;
    
    @JsonProperty("modifiedById")
    private UUID modifiedById;
    
    @JsonProperty("createdByUserName")
    private String createdByUserName;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestRunStatus {
        
        @JsonProperty("id")
        private UUID id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("isSystem")
        private Boolean isSystem;
        
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("description")
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResultDto {
        
        @JsonProperty("id")
        private UUID id;
        
        @JsonProperty("configurationId")
        private UUID configurationId;
        
        @JsonProperty("workItemVersionId")
        private UUID workItemVersionId;
        
        @JsonProperty("autoTestId")
        private UUID autoTestId;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("traces")
        private String traces;
        
        @JsonProperty("startedOn")
        private LocalDateTime startedOn;
        
        @JsonProperty("completedOn")
        private LocalDateTime completedOn;
        
        @JsonProperty("runByUserId")
        private UUID runByUserId;
        
        @JsonProperty("stoppedByUserId")
        private UUID stoppedByUserId;
        
        @JsonProperty("testPointId")
        private UUID testPointId;
        
        @JsonProperty("testRunId")
        private UUID testRunId;
        
        @JsonProperty("outcome")
        private String outcome;
        
        @JsonProperty("comment")
        private String comment;
        
        @JsonProperty("parameters")
        private Map<String, String> parameters;
    }
}
