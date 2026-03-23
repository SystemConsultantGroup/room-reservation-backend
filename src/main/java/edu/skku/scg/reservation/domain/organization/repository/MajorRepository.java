package edu.skku.scg.reservation.domain.organization.repository;

import edu.skku.scg.reservation.domain.organization.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {
}