package com.testit.reports.service;

import com.testit.reports.model.entity.GlobalSetting;
import com.testit.reports.repository.GlobalSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSettingService {

    private final GlobalSettingRepository globalSettingRepository;

    /**
     * Get all global settings
     *
     * @return List of all global settings
     */
    public List<GlobalSetting> getAllSettings() {
        return globalSettingRepository.findAll();
    }

    /**
     * Get global setting by key
     *
     * @param key Setting key
     * @return Global setting
     * @throws EntityNotFoundException if setting not found
     */
    public GlobalSetting getSettingByKey(String key) {
        return globalSettingRepository.findByKey(key)
                .orElseThrow(() -> new EntityNotFoundException("Global setting not found with key: " + key));
    }

    /**
     * Get global setting value by key
     *
     * @param key Setting key
     * @return Setting value
     * @throws EntityNotFoundException if setting not found
     */
    public String getSettingValueByKey(String key) {
        return globalSettingRepository.getValueByKey(key);
    }

    /**
     * Update global setting
     *
     * @param key   Setting key
     * @param value Setting value
     * @return Updated global setting
     * @throws EntityNotFoundException if setting not found
     */
    @Transactional
    public GlobalSetting updateSetting(String key, String value) {
        GlobalSetting setting = getSettingByKey(key);
        setting.setValue(value);
        return globalSettingRepository.save(setting);
    }

    /**
     * Create or update global setting
     *
     * @param key         Setting key
     * @param value       Setting value
     * @param description Setting description
     * @return Created or updated global setting
     */
    @Transactional
    public GlobalSetting createOrUpdateSetting(String key, String value, String description) {
        return globalSettingRepository.findByKey(key)
                .map(setting -> {
                    setting.setValue(value);
                    if (description != null) {
                        setting.setDescription(description);
                    }
                    return globalSettingRepository.save(setting);
                })
                .orElseGet(() -> {
                    GlobalSetting newSetting = new GlobalSetting();
                    newSetting.setKey(key);
                    newSetting.setValue(value);
                    newSetting.setDescription(description);
                    return globalSettingRepository.save(newSetting);
                });
    }

    /**
     * Get global TestIT token
     *
     * @return Global TestIT token
     * @throws EntityNotFoundException if token not found
     */
    public String getGlobalTestItToken() {
        return getSettingValueByKey(GlobalSetting.GLOBAL_TESTIT_TOKEN);
    }

    /**
     * Update global TestIT token
     *
     * @param token Global TestIT token
     * @return Updated global setting
     */
    @Transactional
    public GlobalSetting updateGlobalTestItToken(String token) {
        return updateSetting(GlobalSetting.GLOBAL_TESTIT_TOKEN, token);
    }

    /**
     * Get API schedule cron expression
     *
     * @return API schedule cron expression
     * @throws EntityNotFoundException if setting not found
     */
    public String getApiScheduleCron() {
        return getSettingValueByKey(GlobalSetting.API_SCHEDULE_CRON);
    }

    /**
     * Update API schedule cron expression
     *
     * @param cronExpression Cron expression
     * @return Updated global setting
     */
    @Transactional
    public GlobalSetting updateApiScheduleCron(String cronExpression) {
        return updateSetting(GlobalSetting.API_SCHEDULE_CRON, cronExpression);
    }

    /**
     * Get API base URL
     *
     * @return API base URL
     * @throws EntityNotFoundException if setting not found
     */
    public String getApiBaseUrl() {
        return getSettingValueByKey(GlobalSetting.API_BASE_URL);
    }

    /**
     * Update API base URL
     *
     * @param baseUrl API base URL
     * @return Updated global setting
     */
    @Transactional
    public GlobalSetting updateApiBaseUrl(String baseUrl) {
        return updateSetting(GlobalSetting.API_BASE_URL, baseUrl);
    }

    /**
     * Get TestIT cookies
     *
     * @return TestIT cookies
     * @throws EntityNotFoundException if setting not found
     */
    public String getTestItCookies() {
        return getSettingValueByKey(GlobalSetting.TESTIT_COOKIES);
    }

    /**
     * Update TestIT cookies
     *
     * @param cookies TestIT cookies
     * @return Updated global setting
     */
    @Transactional
    public GlobalSetting updateTestItCookies(String cookies) {
        return updateSetting(GlobalSetting.TESTIT_COOKIES, cookies);
    }

    /**
     * Check if TestIT cookies should be used
     *
     * @return true if TestIT cookies should be used, false otherwise
     */
    public boolean useTestItCookies() {
        String value = getSettingValueByKey(GlobalSetting.USE_TESTIT_COOKIES);
        return Boolean.parseBoolean(value);
    }

    /**
     * Update whether to use TestIT cookies
     *
     * @param use Whether to use TestIT cookies
     * @return Updated global setting
     */
    @Transactional
    public GlobalSetting updateUseTestItCookies(boolean use) {
        return updateSetting(GlobalSetting.USE_TESTIT_COOKIES, String.valueOf(use));
    }
}
