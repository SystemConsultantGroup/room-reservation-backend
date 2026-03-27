package edu.skku.scg.reservation.domain.organization.repository;

import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagementUnitRepository extends JpaRepository<ManagementUnit, Long> {
}