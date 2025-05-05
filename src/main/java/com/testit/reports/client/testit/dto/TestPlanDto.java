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
public class TestPlanDto {
    
    @JsonProperty("analytic")
    private TestPlanAnalytic analytic;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("startedOn")
    private LocalDateTime startedOn;
    
    @JsonProperty("completedOn")
    private LocalDateTime completedOn;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("modifiedDate")
    private LocalDateTime modifiedDate;
    
    @JsonProperty("createdById")
    private UUID createdById;
    
    @JsonProperty("modifiedById")
    private UUID modifiedById;
    
    @JsonProperty("globalId")
    private Integer globalId;
    
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("startDate")
    private LocalDateTime startDate;
    
    @JsonProperty("endDate")
    private LocalDateTime endDate;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("projectId")
    private UUID projectId;
    
    @JsonProperty("attributes")
    private Map<String, Object> attributes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestPlanAnalytic {
        
        @JsonProperty("countGroupByStatus")
        private List<StatusCount> countGroupByStatus;
        
        @JsonProperty("sumGroupByTester")
        private List<Object> sumGroupByTester;
        
        @JsonProperty("countGroupByTester")
        private List<Object> countGroupByTester;
        
        @JsonProperty("countGroupByTestSuite")
        private List<Object> countGroupByTestSuite;
        
        @JsonProperty("countGroupByTesterAndStatus")
        private List<Object> countGroupByTesterAndStatus;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("value")
        private Integer value;
    }
}
