package com.architectai.design;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesignOutputRepository extends JpaRepository<DesignOutput, Long> {
    Optional<DesignOutput> findByProjectId(Long projectId);
}
