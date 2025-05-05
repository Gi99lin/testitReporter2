package com.testit.reports.repository;

import com.testit.reports.model.entity.TestCaseStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestCaseStatisticsRepository extends JpaRepository<TestCaseStatistics, Long> {
    
    Optional<TestCaseStatistics> findByProjectIdAndTestitUserIdAndDate(Long projectId, UUID testitUserId, LocalDate date);
    
    List<TestCaseStatistics> findByProjectIdAndDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT tcs FROM TestCaseStatistics tcs WHERE tcs.project.id = :projectId AND tcs.date BETWEEN :startDate AND :endDate ORDER BY tcs.date")
    List<TestCaseStatistics> findStatisticsByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(tcs.createdCount) FROM TestCaseStatistics tcs WHERE tcs.project.id = :projectId AND tcs.date BETWEEN :startDate AND :endDate")
    Integer getTotalCreatedCountByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(tcs.modifiedCount) FROM TestCaseStatistics tcs WHERE tcs.project.id = :projectId AND tcs.date BETWEEN :startDate AND :endDate")
    Integer getTotalModifiedCountByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM TestCaseStatistics tcs WHERE tcs.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);
}
