package com.testit.reports.repository;

import com.testit.reports.model.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, Long> {
    
    Optional<UserProject> findByUserIdAndProjectId(Long userId, Long projectId);
    
    boolean existsByUserIdAndProjectId(Long userId, Long projectId);
    
    @Modifying
    @Query("UPDATE UserProject up SET up.visible = :visible WHERE up.user.id = :userId AND up.project.id = :projectId")
    void updateVisibility(Long userId, Long projectId, boolean visible);
    
    @Modifying
    @Query("DELETE FROM UserProject up WHERE up.user.id = :userId AND up.project.id = :projectId")
    void deleteByUserIdAndProjectId(Long userId, Long projectId);
    
    @Modifying
    @Query("DELETE FROM UserProject up WHERE up.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);
}
