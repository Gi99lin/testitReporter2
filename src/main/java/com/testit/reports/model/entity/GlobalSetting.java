package com.testit.reports.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "global_settings")
public class GlobalSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String key;

    @Column
    private String value;

    @Column
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Common setting keys
    public static final String GLOBAL_TESTIT_TOKEN = "GLOBAL_TESTIT_TOKEN";
    public static final String API_SCHEDULE_CRON = "API_SCHEDULE_CRON";
    public static final String API_BASE_URL = "API_BASE_URL";
    public static final String TESTIT_COOKIES = "TESTIT_COOKIES";
    public static final String USE_TESTIT_COOKIES = "USE_TESTIT_COOKIES";
}
