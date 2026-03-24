package edu.skku.scg.reservation.domain.reservation.repository;

import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.user " +
            "WHERE r.room.id IN :roomIds " +
            "AND r.startTime < :endTime " +
            "AND r.endTime > :startTime " +
            "ORDER BY r.startTime ASC")
    List<Reservation> findReservationsByRoomIdsAndDate(
            List<Long> roomIds,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.room.id = :roomId " +
            "AND r.startTime < :endTime " +
            "AND r.endTime > :startTime")
    boolean existsOverlappingReservation(
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}