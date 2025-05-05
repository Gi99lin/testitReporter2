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
public class TestPointSearchRequest {
    
    @JsonProperty("testPlanIds")
    private List<UUID> testPlanIds;
    
    @JsonProperty("workItemIsDeleted")
    private Boolean workItemIsDeleted;
    
    @JsonProperty("statuses")
    private List<String> statuses;
    
    @JsonProperty("priorities")
    private List<String> priorities;
    
    @JsonProperty("isAutomated")
    private Boolean isAutomated;
    
    @JsonProperty("testerIds")
    private List<UUID> testerIds;
    
    @JsonProperty("configurationIds")
    private List<UUID> configurationIds;
}
