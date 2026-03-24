package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userMajors um " +
            "LEFT JOIN FETCH um.major " +
            "where u.id = :id")
    Optional<User> findByIdWithMajors(Long id);
}