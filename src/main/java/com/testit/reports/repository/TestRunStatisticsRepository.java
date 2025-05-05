package com.testit.reports.repository;

import com.testit.reports.model.entity.TestRunStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestRunStatisticsRepository extends JpaRepository<TestRunStatistics, Long> {
    
    Optional<TestRunStatistics> findByProjectIdAndTestitUserIdAndDate(Long projectId, UUID testitUserId, LocalDate date);
    
    List<TestRunStatistics> findByProjectIdAndDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT trs FROM TestRunStatistics trs WHERE trs.project.id = :projectId AND trs.date BETWEEN :startDate AND :endDate ORDER BY trs.date")
    List<TestRunStatistics> findStatisticsByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(trs.passedCount) FROM TestRunStatistics trs WHERE trs.project.id = :projectId AND trs.date BETWEEN :startDate AND :endDate")
    Integer getTotalPassedCountByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(trs.failedCount) FROM TestRunStatistics trs WHERE trs.project.id = :projectId AND trs.date BETWEEN :startDate AND :endDate")
    Integer getTotalFailedCountByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);
    
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM TestRunStatistics trs WHERE trs.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);
}
