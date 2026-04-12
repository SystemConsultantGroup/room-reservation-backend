package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.UserMajor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMajorRepository extends JpaRepository<UserMajor, Long> {
}