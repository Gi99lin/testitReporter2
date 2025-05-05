package com.testit.reports.client.testit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemSearchRequest {
    
    @JsonProperty("filter")
    private WorkItemFilter filter;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkItemFilter {
        
        @JsonProperty("projectIds")
        private List<UUID> projectIds;
        
        @JsonProperty("createdByIds")
        private List<UUID> createdByIds;
        
        @JsonProperty("modifiedByIds")
        private List<UUID> modifiedByIds;
        
        @JsonProperty("states")
        private List<String> states;
        
        @JsonProperty("priorities")
        private List<String> priorities;
        
        @JsonProperty("types")
        private List<String> types;
        
        @JsonProperty("createdDate")
        private DateRangeFilter createdDate;
        
        @JsonProperty("modifiedDate")
        private DateRangeFilter modifiedDate;
        
        @JsonProperty("isAutomated")
        private Boolean isAutomated;
        
        @JsonProperty("tags")
        private List<String> tags;
        
        @JsonProperty("attributes")
        private Map<String, List<String>> attributes;
    }
}
