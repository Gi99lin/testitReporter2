package com.testit.reports.repository;

import com.testit.reports.model.entity.TestPointResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestPointResultRepository extends JpaRepository<TestPointResult, Long> {
    
    Optional<TestPointResult> findByTestPointId(UUID testPointId);
    
    boolean existsByTestPointId(UUID testPointId);
    
    @Query("SELECT tpr FROM TestPointResult tpr WHERE tpr.project.id = :projectId AND tpr.date BETWEEN :startDate AND :endDate ORDER BY tpr.date")
    List<TestPointResult> findByProjectIdAndDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT tpr FROM TestPointResult tpr WHERE tpr.project.id = :projectId AND tpr.testitUserId = :userId AND tpr.date BETWEEN :startDate AND :endDate ORDER BY tpr.date")
    List<TestPointResult> findByProjectIdAndUserIdAndDateBetween(Long projectId, UUID userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(tpr) FROM TestPointResult tpr WHERE tpr.project.id = :projectId AND tpr.date BETWEEN :startDate AND :endDate AND tpr.status = 'Passed'")
    Integer countPassedByProjectIdAndDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(tpr) FROM TestPointResult tpr WHERE tpr.project.id = :projectId AND tpr.date BETWEEN :startDate AND :endDate AND tpr.status = 'Failed'")
    Integer countFailedByProjectIdAndDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(tpr) FROM TestPointResult tpr WHERE tpr.project.id = :projectId AND tpr.testitUserId = :userId AND tpr.date BETWEEN :startDate AND :endDate AND tpr.status = 'Passed'")
    Integer countPassedByProjectIdAndUserIdAndDateBetween(Long projectId, UUID userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(tpr) FROM TestPointResult tpr WHERE tpr.project.id = :projectId AND tpr.testitUserId = :userId AND tpr.date BETWEEN :startDate AND :endDate AND tpr.status = 'Failed'")
    Integer countFailedByProjectIdAndUserIdAndDateBetween(Long projectId, UUID userId, LocalDate startDate, LocalDate endDate);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM TestPointResult tpr WHERE tpr.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);
}
