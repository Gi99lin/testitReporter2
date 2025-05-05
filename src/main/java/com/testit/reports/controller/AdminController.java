package com.testit.reports.controller;

import com.testit.reports.controller.dto.ApiResponse;
import com.testit.reports.controller.dto.GlobalSettingDto;
import com.testit.reports.model.entity.GlobalSetting;
import com.testit.reports.service.GlobalSettingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final GlobalSettingService globalSettingService;

    /**
     * Get all global settings
     *
     * @return List of global settings
     */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<GlobalSettingDto>>> getAllSettings() {
        try {
            List<GlobalSettingDto> settings = globalSettingService.getAllSettings().stream()
                    .map(GlobalSettingDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(settings));
        } catch (Exception e) {
            log.error("Error getting all settings", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get global setting by key
     *
     * @param key Setting key
     * @return Global setting
     */
    @GetMapping("/settings/{key}")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> getSettingByKey(@PathVariable String key) {
        try {
            GlobalSetting setting = globalSettingService.getSettingByKey(key);
            return ResponseEntity.ok(ApiResponse.success(GlobalSettingDto.fromEntity(setting)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting setting by key", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update global setting
     *
     * @param key       Setting key
     * @param settingDto Setting data
     * @return Updated global setting
     */
    @PutMapping("/settings/{key}")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody GlobalSettingDto settingDto) {
        try {
            // Ensure key in path and body match
            if (!key.equals(settingDto.getKey())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Key in path and body must match"));
            }

            GlobalSetting setting = globalSettingService.updateSetting(key, settingDto.getValue());
            return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", GlobalSettingDto.fromEntity(setting)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating setting", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create or update global setting
     *
     * @param settingDto Setting data
     * @return Created or updated global setting
     */
    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> createOrUpdateSetting(@Valid @RequestBody GlobalSettingDto settingDto) {
        try {
            GlobalSetting setting = globalSettingService.createOrUpdateSetting(
                    settingDto.getKey(),
                    settingDto.getValue(),
                    settingDto.getDescription());
            return ResponseEntity.ok(ApiResponse.success("Setting created or updated successfully", GlobalSettingDto.fromEntity(setting)));
        } catch (Exception e) {
            log.error("Error creating or updating setting", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update global TestIT token
     *
     * @param token Global TestIT token
     * @return Updated global setting
     */
    @PutMapping("/settings/global-token")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> updateGlobalTestItToken(@RequestBody String token) {
        try {
            // Remove quotes if present (happens when sending raw string in request body)
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            
            GlobalSetting setting = globalSettingService.updateGlobalTestItToken(token);
            return ResponseEntity.ok(ApiResponse.success("Global TestIT token updated successfully", GlobalSettingDto.fromEntity(setting)));
        } catch (Exception e) {
            log.error("Error updating global TestIT token", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update API schedule cron expression
     *
     * @param cronExpression Cron expression
     * @return Updated global setting
     */
    @PutMapping("/settings/api-schedule")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> updateApiScheduleCron(@RequestBody String cronExpression) {
        try {
            // Remove quotes if present (happens when sending raw string in request body)
            if (cronExpression.startsWith("\"") && cronExpression.endsWith("\"")) {
                cronExpression = cronExpression.substring(1, cronExpression.length() - 1);
            }
            
            GlobalSetting setting = globalSettingService.updateApiScheduleCron(cronExpression);
            return ResponseEntity.ok(ApiResponse.success("API schedule cron expression updated successfully", GlobalSettingDto.fromEntity(setting)));
        } catch (Exception e) {
            log.error("Error updating API schedule cron expression", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update API base URL
     *
     * @param baseUrl API base URL
     * @return Updated global setting
     */
    @PutMapping("/settings/api-base-url")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> updateApiBaseUrl(@RequestBody String baseUrl) {
        try {
            // Remove quotes if present (happens when sending raw string in request body)
            if (baseUrl.startsWith("\"") && baseUrl.endsWith("\"")) {
                baseUrl = baseUrl.substring(1, baseUrl.length() - 1);
            }
            
            GlobalSetting setting = globalSettingService.updateApiBaseUrl(baseUrl);
            return ResponseEntity.ok(ApiResponse.success("API base URL updated successfully", GlobalSettingDto.fromEntity(setting)));
        } catch (Exception e) {
            log.error("Error updating API base URL", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Update TestIT cookies
     *
     * @param cookies TestIT cookies
     * @return Updated global setting
     */
    @PutMapping("/settings/testit-cookies")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> updateTestItCookies(@RequestBody String cookies) {
        try {
            // Remove quotes if present (happens when sending raw string in request body)
            if (cookies.startsWith("\"") && cookies.endsWith("\"")) {
                cookies = cookies.substring(1, cookies.length() - 1);
            }
            
            GlobalSetting setting = globalSettingService.updateTestItCookies(cookies);
            return ResponseEntity.ok(ApiResponse.success("TestIT cookies updated successfully", GlobalSettingDto.fromEntity(setting)));
        } catch (Exception e) {
            log.error("Error updating TestIT cookies", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Toggle using TestIT cookies
     *
     * @param use Whether to use TestIT cookies
     * @return Updated global setting
     */
    @PutMapping("/settings/use-testit-cookies")
    public ResponseEntity<ApiResponse<GlobalSettingDto>> toggleUseTestItCookies(@RequestBody boolean use) {
        try {
            GlobalSetting setting = globalSettingService.updateUseTestItCookies(use);
            return ResponseEntity.ok(ApiResponse.success(
                    "TestIT cookies " + (use ? "enabled" : "disabled") + " successfully", 
                    GlobalSettingDto.fromEntity(setting)));
        } catch (Exception e) {
            log.error("Error toggling TestIT cookies", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
