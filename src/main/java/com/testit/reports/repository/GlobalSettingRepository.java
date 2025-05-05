package com.testit.reports.repository;

import com.testit.reports.model.entity.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalSettingRepository extends JpaRepository<GlobalSetting, Long> {
    
    Optional<GlobalSetting> findByKey(String key);
    
    boolean existsByKey(String key);
    
    @Modifying
    @Query("UPDATE GlobalSetting gs SET gs.value = :value WHERE gs.key = :key")
    void updateValueByKey(String key, String value);
    
    @Query("SELECT gs.value FROM GlobalSetting gs WHERE gs.key = :key")
    String getValueByKey(String key);
}
