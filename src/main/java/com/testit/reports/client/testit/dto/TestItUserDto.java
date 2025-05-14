package com.testit.reports.client.testit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for TestIT user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestItUserDto {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("middleName")
    private String middleName;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("displayName")
    private String displayName;
    
    @JsonProperty("userType")
    private String userType;
    
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    
    @JsonProperty("isDisabled")
    private Boolean isDisabled;
    
    @JsonProperty("providerId")
    private UUID providerId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("phoneNumber")
    private String phoneNumber;
    
    @JsonProperty("isAdmin")
    private Boolean isAdmin;
    
    @JsonProperty("isOwner")
    private Boolean isOwner;
    
    @JsonProperty("createdDate")
    private String createdDate;
    
    @JsonProperty("modifiedDate")
    private String modifiedDate;
    
    @JsonProperty("birthday")
    private String birthday;
    
    @JsonProperty("firstLogon")
    private String firstLogon;
    
    @JsonProperty("lastLoggedIn")
    private String lastLoggedIn;
    
    @JsonProperty("permissions")
    private List<String> permissions;
    
    @JsonProperty("roles")
    private List<String> roles;
    
    @JsonProperty("tenantId")
    private UUID tenantId;
    
    @JsonProperty("scheme")
    private String scheme;
    
    @JsonProperty("isActiveStatusByEntity")
    private Boolean isActiveStatusByEntity;
    
    @JsonProperty("userRank")
    private UserRank userRank;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRank {
        
        @JsonProperty("score")
        private Integer score;
        
        @JsonProperty("workItemsCreated")
        private Integer workItemsCreated;
        
        @JsonProperty("passedTestPoints")
        private Integer passedTestPoints;
        
        @JsonProperty("failedTestPoints")
        private Integer failedTestPoints;
        
        @JsonProperty("skippedTestPoints")
        private Integer skippedTestPoints;
        
        @JsonProperty("blockedTestPoints")
        private Integer blockedTestPoints;
        
        @JsonProperty("levelAvatarEnabled")
        private Boolean levelAvatarEnabled;
    }
}
