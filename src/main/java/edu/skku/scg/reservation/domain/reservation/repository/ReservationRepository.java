package edu.skku.scg.reservation.domain.reservation.repository;

import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.user " +
            "WHERE r.room.id = :roomId " +
            "AND r.startTime < :endTime " +
            "AND r.endTime > :startTime " +
            "ORDER BY r.startTime ASC")
    List<Reservation> findReservationsByRoomIdAndDate(
            Long roomId,
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

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.room " +
            "JOIN FETCH r.user " +
            "WHERE r.user.id = :userId " +
            "AND r.endTime >= :time " +
            "ORDER BY r.startTime ASC")
    List<Reservation> findReservationsByUserIdAndTimeAfter(
            Long userId,
            LocalDateTime time
    );

    @Query("SELECT DISTINCT r FROM Reservation r " +
            "JOIN FETCH r.user " +
            "JOIN FETCH r.room room " +
            "LEFT JOIN FETCH room.majorRooms mr " +
            "LEFT JOIN FETCH mr.major major " +
            "LEFT JOIN FETCH major.managementUnit " +
            "WHERE r.id = :reservationId"
    )
    Optional<Reservation> findByIdWithRoomMajors(Long reservationId);

    @Query(
            value = "SELECT r FROM Reservation r " +
                    "JOIN FETCH r.user " +
                    "WHERE r.room.id = :roomId " +
                    "AND r.endTime >= :time " +
                    "ORDER BY r.startTime ASC",
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "WHERE r.room.id = :roomId " +
                    "AND r.endTime >= :time"
    )
    Page<Reservation> findFutureReservationsByRoomId(
            Long roomId,
            LocalDateTime time,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Reservation r " +
            "WHERE r.user.id = :userId " +
            "AND r.endTime >= :time " +
            "AND r.room.id IN :roomIds")
    void deleteFutureReservationsByUserIdAndRoomIds(
            Long userId,
            LocalDateTime time,
            List<Long> roomIds
    );
}
