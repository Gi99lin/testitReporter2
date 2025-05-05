package com.testit.reports.client.testit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPointDto {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("createdById")
    private UUID createdById;
    
    @JsonProperty("modifiedDate")
    private LocalDateTime modifiedDate;
    
    @JsonProperty("modifiedById")
    private UUID modifiedById;
    
    @JsonProperty("testerId")
    private UUID testerId;
    
    @JsonProperty("parameters")
    private Map<String, String> parameters;
    
    @JsonProperty("attributes")
    private Map<String, String> attributes;
    
    @JsonProperty("testSuiteId")
    private UUID testSuiteId;
    
    @JsonProperty("testSuiteName")
    private String testSuiteName;
    
    @JsonProperty("workItemId")
    private UUID workItemId;
    
    @JsonProperty("workItemGlobalId")
    private Integer workItemGlobalId;
    
    @JsonProperty("workItemVersionId")
    private UUID workItemVersionId;
    
    @JsonProperty("workItemVersionNumber")
    private Integer workItemVersionNumber;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("statusModel")
    private StatusModel statusModel;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("isAutomated")
    private Boolean isAutomated;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("configurationId")
    private UUID configurationId;
    
    @JsonProperty("duration")
    private Long duration;
    
    @JsonProperty("sectionId")
    private UUID sectionId;
    
    @JsonProperty("sectionName")
    private String sectionName;
    
    @JsonProperty("projectId")
    private UUID projectId;
    
    @JsonProperty("lastTestResult")
    private LastTestResult lastTestResult;
    
    @JsonProperty("workItemCreatedById")
    private UUID workItemCreatedById;
    
    @JsonProperty("workItemCreatedDate")
    private LocalDateTime workItemCreatedDate;
    
    @JsonProperty("workItemModifiedById")
    private UUID workItemModifiedById;
    
    @JsonProperty("workItemModifiedDate")
    private LocalDateTime workItemModifiedDate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusModel {
        
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
    public static class LastTestResult {
        
        @JsonProperty("id")
        private UUID id;
        
        @JsonProperty("testRunId")
        private UUID testRunId;
        
        @JsonProperty("autoTestId")
        private UUID autoTestId;
        
        @JsonProperty("comment")
        private String comment;
        
        @JsonProperty("workItemVersionId")
        private UUID workItemVersionId;
    }
}
