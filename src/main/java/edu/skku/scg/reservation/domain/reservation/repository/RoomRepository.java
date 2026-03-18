package edu.skku.scg.reservation.domain.reservation.repository;

import edu.skku.scg.reservation.domain.reservation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}