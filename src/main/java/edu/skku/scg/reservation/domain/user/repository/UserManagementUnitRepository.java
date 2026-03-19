package edu.skku.scg.reservation.domain.user.repository;

import edu.skku.scg.reservation.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserManagementUnitRepository extends JpaRepository<User, Long> {

    @Query("SELECT umu.managementUnit.id " +
            "FROM UserManagementUnit umu " +
            "WHERE umu.user.id = :userId")
    List<Long> findAllManagedUnitIdsByUserId(Long userId);
}