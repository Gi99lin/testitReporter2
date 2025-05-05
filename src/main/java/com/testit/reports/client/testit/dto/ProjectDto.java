package com.testit.reports.client.testit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    
    @JsonProperty("id")
    private Object id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    
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
    
    @JsonProperty("isFavorite")
    private Boolean isFavorite;
    
    @JsonProperty("attributesSchemaId")
    private UUID attributesSchemaId;
    
    @JsonProperty("isAutomatic")
    private Boolean isAutomatic;
}
