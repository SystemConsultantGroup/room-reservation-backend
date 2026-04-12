package edu.skku.scg.reservation.domain.organization.repository;

import edu.skku.scg.reservation.domain.organization.entity.OriginManagementUnit;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OriginManagementUnitRepository extends JpaRepository<OriginManagementUnit, Long> {

    @Cacheable(cacheNames = "originManagementUnitId", key = "#originUrl")
    @Query("SELECT o.managementUnit.id FROM OriginManagementUnit o WHERE o.originUrl = :originUrl")
    Optional<Long> findManagementUnitIdByOriginUrl(String originUrl);
}