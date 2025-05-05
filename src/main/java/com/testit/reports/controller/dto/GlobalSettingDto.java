package com.testit.reports.controller.dto;

import com.testit.reports.model.entity.GlobalSetting;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSettingDto {
    
    private Long id;
    
    @NotBlank(message = "Key is required")
    private String key;
    
    private String value;
    
    private String description;
    
    /**
     * Convert GlobalSetting entity to GlobalSettingDto
     *
     * @param setting GlobalSetting entity
     * @return GlobalSettingDto
     */
    public static GlobalSettingDto fromEntity(GlobalSetting setting) {
        return GlobalSettingDto.builder()
                .id(setting.getId())
                .key(setting.getKey())
                .value(setting.getValue())
                .description(setting.getDescription())
                .build();
    }
    
    /**
     * Convert GlobalSettingDto to GlobalSetting entity
     *
     * @return GlobalSetting entity
     */
    public GlobalSetting toEntity() {
        GlobalSetting setting = new GlobalSetting();
        setting.setId(this.id);
        setting.setKey(this.key);
        setting.setValue(this.value);
        setting.setDescription(this.description);
        return setting;
    }
}
