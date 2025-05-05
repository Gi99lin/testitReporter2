package com.testit.reports.client.testit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRunSearchRequest {
    
    @JsonProperty("filter")
    private TestRunFilter filter;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestRunFilter {
        
        @JsonProperty("projectIds")
        private List<UUID> projectIds;
        
        @JsonProperty("testPlanIds")
        private List<UUID> testPlanIds;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("states")
        private List<String> states;
        
        @JsonProperty("createdDate")
        private DateRangeFilter createdDate;
        
        @JsonProperty("startedDate")
        private DateRangeFilter startedDate;
        
        @JsonProperty("completedDate")
        private DateRangeFilter completedDate;
        
        @JsonProperty("createdByIds")
        private List<UUID> createdByIds;
        
        @JsonProperty("modifiedByIds")
        private List<UUID> modifiedByIds;
    }
}
