package com.testit.reports.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test_run_statistics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "testit_user_id", "date"})
})
public class TestRunStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "testit_user_id", nullable = false)
    private UUID testitUserId;

    @Column(name = "testit_username", nullable = false, length = 100)
    private String testitUsername;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "passed_count", nullable = false)
    private int passedCount = 0;

    @Column(name = "failed_count", nullable = false)
    private int failedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
