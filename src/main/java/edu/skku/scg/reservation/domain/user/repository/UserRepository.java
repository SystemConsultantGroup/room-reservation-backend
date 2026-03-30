package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import edu.skku.scg.reservation.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userMajors um " +
            "LEFT JOIN FETCH um.major " +
            "where u.id = :id")
    Optional<User> findByIdWithMajors(Long id);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.userMajors um " +
            "JOIN um.major m " +
            "WHERE m.managementUnit.id IN :managementUnitIds " +
            "AND um.status = :status " +
            "AND (:keyword IS NULL OR " +
                "u.name LIKE %:keyword% OR " +
                "u.studentId LIKE %:keyword% OR " +
                "u.email LIKE %:keyword%)")
    Page<User> findUsersByUnitIdsAndRegistrationStatus(List<Long> managementUnitIds, RegistrationStatus status, Pageable pageable, String keyword);
}