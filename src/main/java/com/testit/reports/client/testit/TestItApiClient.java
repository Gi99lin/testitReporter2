package com.testit.reports.client.testit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testit.reports.client.testit.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import com.testit.reports.service.GlobalSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestItApiClient {

    private final WebClient testItWebClient;
    private final GlobalSettingService globalSettingService;
    private final ObjectMapper objectMapper;
    
    /**
     * Add headers to the request
     * 
     * @param headersSpec The headers specification
     * @param token The authentication token
     */
    private void addHeaders(WebClient.RequestHeadersSpec<?> headersSpec, String token) {
        // Remove quotes if present (happens when token was saved with quotes)
        if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
            token = token.substring(1, token.length() - 1);
            log.info("Removed quotes from token");
        }
        
        log.info("Adding Authorization header: {}", token);
        headersSpec.header(HttpHeaders.AUTHORIZATION, token);
        
        // Add cookies if enabled
        if (globalSettingService.useTestItCookies()) {
            String cookies = globalSettingService.getTestItCookies();
            if (cookies != null && !cookies.isEmpty()) {
                headersSpec.header(HttpHeaders.COOKIE, cookies);
                log.info("Added cookies to request");
            }
        }
    }

    /**
     * Search for work items (test cases) with the given filter
     *
     * @param token  The authentication token
     * @param request The search request
     * @return List of work items
     */
    public List<WorkItemDto> searchWorkItems(String token, WorkItemSearchRequest request) {
        log.info("Searching for work items with token: {}", token);
        log.info("Request: {}", request);
        
        // Convert request to JSON for logging
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(request);
            log.info("Request JSON: {}", requestJson);
        } catch (Exception e) {
            log.error("Error converting request to JSON", e);
            requestJson = request.toString();
        }
        
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.post()
                .uri("/workItems/search")
                .bodyValue(request);
        
        // Don't add "Bearer " prefix if token already has a prefix
        String finalToken;
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            log.info("Token already has prefix, using as is");
            finalToken = token;
        } else {
            log.info("Adding Bearer prefix to token");
            finalToken = "Bearer " + token;
        }
        
        addHeaders(headersSpec, finalToken);
        
        // Log the full curl command that can be used to reproduce the request
        log.info("Equivalent curl command: curl -X POST '{}{}' -H 'Content-Type: application/json' -H 'Authorization: {}' -d '{}'",
                globalSettingService.getApiBaseUrl(), "/workItems/search", finalToken, requestJson);
        
        try {
            List<WorkItemDto> result = headersSpec.retrieve()
                    .bodyToFlux(WorkItemDto.class)
                    .collectList()
                    .block();
            
            log.info("Received {} work items", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Error searching for work items: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Search for test runs with the given filter
     *
     * @param token  The authentication token
     * @param request The search request
     * @return List of test runs
     */
    public List<TestRunDto> searchTestRuns(String token, TestRunSearchRequest request) {
        log.info("Searching for test runs with token: {}", token);
        log.info("Request: {}", request);
        
        // Convert request to JSON for logging
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(request);
            log.info("Request JSON: {}", requestJson);
        } catch (Exception e) {
            log.error("Error converting request to JSON", e);
            requestJson = request.toString();
        }
        
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.post()
                .uri("/testRuns/search")
                .bodyValue(request);
        
        // Don't add "Bearer " prefix if token already has a prefix
        String finalToken;
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            log.info("Token already has prefix, using as is");
            finalToken = token;
        } else {
            log.info("Adding Bearer prefix to token");
            finalToken = "Bearer " + token;
        }
        
        addHeaders(headersSpec, finalToken);
        
        // Log the full curl command that can be used to reproduce the request
        log.info("Equivalent curl command: curl -X POST '{}{}' -H 'Content-Type: application/json' -H 'Authorization: {}' -d '{}'",
                globalSettingService.getApiBaseUrl(), "/testRuns/search", finalToken, requestJson);
        
        try {
            List<TestRunDto> result = headersSpec.retrieve()
                    .bodyToFlux(TestRunDto.class)
                    .collectList()
                    .block();
            
            log.info("Received {} test runs", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Error searching for test runs: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get a test run by ID
     *
     * @param token  The authentication token
     * @param testRunId The test run ID
     * @return The test run
     */
    public TestRunDto getTestRun(String token, UUID testRunId) {
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.get()
                .uri("/testRuns/{id}", testRunId);
        
        // Don't add "Bearer " prefix if token already has a prefix
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            addHeaders(headersSpec, token);
        } else {
            addHeaders(headersSpec, "Bearer " + token);
        }
        
        return headersSpec.retrieve()
                .bodyToMono(TestRunDto.class)
                .block();
    }

    /**
     * Get project by UUID
     *
     * @param token     The authentication token
     * @param projectId The project UUID
     * @return The project
     */
    public Mono<ProjectDto> getProject(String token, UUID projectId) {
        log.info("Getting project by UUID: {}", projectId);
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.get()
                .uri("/projects/{id}", projectId);
        
        // Don't add "Bearer " prefix if token already has a prefix
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            addHeaders(headersSpec, token);
        } else {
            addHeaders(headersSpec, "Bearer " + token);
        }
        
        return headersSpec.retrieve()
                .bodyToMono(ProjectDto.class);
    }
    
    /**
     * Get project by numeric ID
     *
     * @param token     The authentication token
     * @param projectId The numeric project ID
     * @return The project
     */
    public Mono<ProjectDto> getProjectById(String token, Long projectId) {
        log.info("Getting project by numeric ID: {}", projectId);
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.get()
                .uri("/projects/{id}", projectId);
        
        // Don't add "Bearer " prefix if token already has a prefix
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            addHeaders(headersSpec, token);
        } else {
            addHeaders(headersSpec, "Bearer " + token);
        }
        
        return headersSpec.retrieve()
                .bodyToMono(ProjectDto.class);
    }

    /**
     * Get all projects
     *
     * @param token The authentication token
     * @return List of projects
     */
    public List<ProjectDto> getAllProjects(String token) {
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.get()
                .uri("/projects");
        
        // Don't add "Bearer " prefix if token already has a prefix
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            addHeaders(headersSpec, token);
        } else {
            addHeaders(headersSpec, "Bearer " + token);
        }
        
        return headersSpec.retrieve()
                .bodyToFlux(ProjectDto.class)
                .collectList()
                .block();
    }
    
    /**
     * Get test plans for a project
     *
     * @param token     The authentication token
     * @param projectId The project UUID
     * @return List of test plans
     */
    public List<TestPlanDto> getProjectTestPlans(String token, UUID projectId) {
        log.info("Getting test plans for project: {}", projectId);
        
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.get()
                .uri("/projects/{projectId}/testPlans/analytics?mustUpdateCache=false", projectId);
        
        // Don't add "Bearer " prefix if token already has a prefix
        String finalToken;
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            log.info("Token already has prefix, using as is");
            finalToken = token;
        } else {
            log.info("Adding Bearer prefix to token");
            finalToken = "Bearer " + token;
        }
        
        addHeaders(headersSpec, finalToken);
        
        // Log the full curl command that can be used to reproduce the request
        log.info("Equivalent curl command: curl -X GET '{}{}' -H 'Content-Type: application/json' -H 'Authorization: {}'",
                globalSettingService.getApiBaseUrl(), 
                "/projects/" + projectId + "/testPlans/analytics?mustUpdateCache=false", 
                finalToken);
        
        try {
            List<TestPlanDto> result = headersSpec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TestPlanDto>>() {})
                    .block();
            
            log.info("Received {} test plans", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Error getting test plans: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Search for test points with the given filter
     *
     * @param token  The authentication token
     * @param request The search request
     * @param skip    Number of items to skip
     * @param take    Number of items to take
     * @return List of test points
     */
    public List<TestPointDto> searchTestPoints(String token, TestPointSearchRequest request, int skip, int take) {
        log.info("Searching for test points with token: {}", token);
        log.info("Request: {}, skip: {}, take: {}", request, skip, take);
        
        // Convert request to JSON for logging
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(request);
            log.info("Request JSON: {}", requestJson);
        } catch (Exception e) {
            log.error("Error converting request to JSON", e);
            requestJson = request.toString();
        }
        
        WebClient.RequestHeadersSpec<?> headersSpec = testItWebClient.post()
                .uri("/testPoints/search?skip={skip}&take={take}", skip, take)
                .bodyValue(request);
        
        // Don't add "Bearer " prefix if token already has a prefix
        String finalToken;
        if (token.startsWith("OpenIdConnect") || token.startsWith("Bearer")) {
            log.info("Token already has prefix, using as is");
            finalToken = token;
        } else {
            log.info("Adding Bearer prefix to token");
            finalToken = "Bearer " + token;
        }
        
        addHeaders(headersSpec, finalToken);
        
        // Log the full curl command that can be used to reproduce the request
        log.info("Equivalent curl command: curl -X POST '{}{}' -H 'Content-Type: application/json' -H 'Authorization: {}' -d '{}'",
                globalSettingService.getApiBaseUrl(), 
                "/testPoints/search?skip=" + skip + "&take=" + take, 
                finalToken, 
                requestJson);
        
        try {
            List<TestPointDto> result = headersSpec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TestPointDto>>() {})
                    .block();
            
            log.info("Received {} test points", result != null ? result.size() : 0);
            
            // Log details of each test point for debugging
            if (result != null && !result.isEmpty()) {
                for (int i = 0; i < Math.min(result.size(), 5); i++) {  // Log at most 5 test points to avoid flooding logs
                    TestPointDto testPoint = result.get(i);
                    log.info("Test point {}: ID={}, Status={}, ModifiedById={}, CreatedById={}, ProjectId={}", 
                            i, testPoint.getId(), testPoint.getStatus(), 
                            testPoint.getModifiedById(), testPoint.getCreatedById(), 
                            testPoint.getProjectId());
                }
                
                // Count statuses
                Map<String, Long> statusCounts = result.stream()
                        .collect(Collectors.groupingBy(
                                TestPointDto::getStatus,
                                Collectors.counting()
                        ));
                
                log.info("Status counts: {}", statusCounts);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error searching for test points: {}", e.getMessage(), e);
            throw e;
        }
    }
}
