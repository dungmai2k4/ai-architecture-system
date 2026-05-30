package com.architectai.design.repository;

import com.architectai.design.domain.DesignProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignRepository extends JpaRepository<DesignProject, Long> {
}
