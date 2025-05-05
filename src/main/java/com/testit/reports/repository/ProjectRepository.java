package com.testit.reports.repository;

import com.testit.reports.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    Optional<Project> findByTestitId(UUID testitId);
    
    boolean existsByTestitId(UUID testitId);
    
    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE'")
    List<Project> findAllActive();
    
    @Query("SELECT p FROM Project p JOIN p.userProjects up WHERE up.user.id = :userId AND up.visible = true")
    List<Project> findVisibleProjectsByUserId(Long userId);
    
    @Query("SELECT p FROM Project p JOIN p.userProjects up WHERE up.user.id = :userId")
    List<Project> findAllProjectsByUserId(Long userId);
}
