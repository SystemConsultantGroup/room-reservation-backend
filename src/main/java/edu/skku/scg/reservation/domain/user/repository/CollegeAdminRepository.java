package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.CollegeAdmin;
import edu.skku.scg.reservation.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollegeAdminRepository extends JpaRepository<CollegeAdmin, Long> {

    @Query("select ca from CollegeAdmin ca join fetch ca.college where ca.admin.id = :adminId")
    List<CollegeAdmin> findAllByAdminId(Long adminId);
}