package edu.skku.scg.reservation.domain.room.repository;

import edu.skku.scg.reservation.domain.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT DISTINCT r FROM Room r " +
            "JOIN MajorRoom mr ON r.id = mr.room.id " +
            "JOIN Major m ON mr.major.id = m.id " +
            "WHERE m.managementUnit.id = :managementUnitId")
    Page<Room> findRoomsByManagementUnitId(Long managementUnitId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Room r " +
            "LEFT JOIN FETCH r.majorRooms mr " +
            "LEFT JOIN FETCH mr.major " +
            "WHERE r.id = :roomId")
    Optional<Room> findByIdWithMajors(Long roomId);
}