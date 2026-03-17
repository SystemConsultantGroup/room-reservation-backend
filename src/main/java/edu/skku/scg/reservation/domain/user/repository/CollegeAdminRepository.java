package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.CollegeAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollegeAdminRepository extends JpaRepository<CollegeAdmin, Long> {

    List<CollegeAdmin> findAllByAdminId(Long adminId);
}