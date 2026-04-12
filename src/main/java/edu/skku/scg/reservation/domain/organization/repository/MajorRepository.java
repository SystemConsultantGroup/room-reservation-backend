package edu.skku.scg.reservation.domain.organization.repository;

import edu.skku.scg.reservation.domain.organization.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major, Long> {

    List<Major> findAllByManagementUnitId(Long managementUnitId);

    List<Major> findAllByManagementUnitIdIn(List<Long> managementUnitIds);
}