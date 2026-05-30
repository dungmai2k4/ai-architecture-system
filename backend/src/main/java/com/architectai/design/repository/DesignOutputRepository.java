package com.architectai.design.repository;

import com.architectai.design.domain.DesignOutput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesignOutputRepository extends JpaRepository<DesignOutput, Long> {
    Optional<DesignOutput> findByProjectId(Long projectId);

    List<DesignOutput> findTop8ByOrderByUpdatedAtDesc();
}
