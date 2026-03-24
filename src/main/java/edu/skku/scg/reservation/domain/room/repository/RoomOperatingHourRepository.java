package edu.skku.scg.reservation.domain.room.repository;

import edu.skku.scg.reservation.domain.room.entity.RoomOperatingHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface RoomOperatingHourRepository extends JpaRepository<RoomOperatingHour, Long> {
    Optional<RoomOperatingHour> findByRoomIdAndDayOfWeek(Long roomId, DayOfWeek dayOfWeek);
}