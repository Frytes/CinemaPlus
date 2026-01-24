package com.frytes.cinemaPlus.audit.repository;

import com.frytes.cinemaPlus.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}