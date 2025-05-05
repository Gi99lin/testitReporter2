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
public class WorkItemDto {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("versionId")
    private UUID versionId;
    
    @JsonProperty("versionNumber")
    private Integer versionNumber;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("entityTypeName")
    private String entityTypeName;
    
    @JsonProperty("projectId")
    private UUID projectId;
    
    @JsonProperty("sectionId")
    private UUID sectionId;
    
    @JsonProperty("sectionName")
    private String sectionName;
    
    @JsonProperty("isAutomated")
    private Boolean isAutomated;
    
    @JsonProperty("globalId")
    private Integer globalId;
    
    @JsonProperty("duration")
    private Integer duration;
    
    @JsonProperty("medianDuration")
    private Integer medianDuration;
    
    @JsonProperty("attributes")
    private Map<String, String> attributes;
    
    @JsonProperty("createdById")
    private UUID createdById;
    
    @JsonProperty("modifiedById")
    private UUID modifiedById;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("modifiedDate")
    private LocalDateTime modifiedDate;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
}
