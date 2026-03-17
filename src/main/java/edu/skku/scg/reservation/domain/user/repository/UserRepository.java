package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGoogleId(String googleId);

    @Query("select u from User u " +
            "left join fetch u.collegeAdmins " +
            "left join fetch u.collegeMemberships " +
            "where u.id = :userId")
    Optional<User> findByIdWithMembershipsAndAdmins(Long userId);
}