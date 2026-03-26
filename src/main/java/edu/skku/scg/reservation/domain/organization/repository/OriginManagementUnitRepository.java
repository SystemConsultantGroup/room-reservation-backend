package edu.skku.scg.reservation.domain.organization.repository;

import edu.skku.scg.reservation.domain.organization.entity.OriginManagementUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OriginManagementUnitRepository extends JpaRepository<OriginManagementUnit, Long> {
    Optional<OriginManagementUnit> findByOriginUrl(String originUrl);
}