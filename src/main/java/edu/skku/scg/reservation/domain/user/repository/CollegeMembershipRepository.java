package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.CollegeMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollegeMembershipRepository extends JpaRepository<CollegeMembership, Long> {

    List<CollegeMembership> findAllByUserId(Long userId);
}