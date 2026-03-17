package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.CollegeMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollegeMembershipRepository extends JpaRepository<CollegeMembership, Long> {

    @Query("select cm from CollegeMembership cm join fetch cm.college where cm.user.id = :userId")
    List<CollegeMembership> findAllByUserId(Long userId);
}