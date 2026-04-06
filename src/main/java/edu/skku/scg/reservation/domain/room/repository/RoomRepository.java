package edu.skku.scg.reservation.domain.room.repository;

import edu.skku.scg.reservation.domain.room.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT DISTINCT r FROM Room r " +
            "JOIN r.majorRooms mr " +
            "JOIN mr.major m " +
            "WHERE m.managementUnit.id = :managementUnitId")
    Page<Room> findRoomsByManagementUnitId(Long managementUnitId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Room r " +
            "JOIN r.majorRooms mr " +
            "JOIN mr.major m " +
            "WHERE m.managementUnit.id IN :managementUnitIds " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM MajorRoom mr2 " +
                "JOIN mr2.major m2 " +
                "WHERE mr2.room = r " +
                "AND m2.managementUnit.id NOT IN :managementUnitIds" +
            ")")
    Page<Room> findRoomsByManagementUnitIds(List<Long> managementUnitIds, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Room r " +
            "JOIN MajorRoom mr ON r.id = mr.room.id " +
            "JOIN Major m ON mr.major.id = m.id " +
            "WHERE m.managementUnit.id = :managementUnitId " +
            "ORDER BY r.id ASC")
    List<Room> findAllByManagementUnitId(Long managementUnitId);

    @Query("SELECT DISTINCT r FROM Room r " +
            "LEFT JOIN FETCH r.majorRooms mr " +
            "LEFT JOIN FETCH mr.major m " +
            "WHERE m.managementUnit.id = :managementUnitId " +
            "ORDER BY r.id ASC")
    List<Room> findAllByManagementUnitIdWithMajors(Long managementUnitId);

    @Query("SELECT DISTINCT r FROM Room r " +
            "LEFT JOIN FETCH r.majorRooms mr " +
            "LEFT JOIN FETCH mr.major " +
            "WHERE r.id = :roomId")
    Optional<Room> findByIdWithMajors(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT r FROM Room r " +
            "LEFT JOIN FETCH r.majorRooms mr " +
            "LEFT JOIN FETCH mr.major " +
            "WHERE r.id = :roomId")
    Optional<Room> findByIdWithMajorsAndLock(Long roomId);
}