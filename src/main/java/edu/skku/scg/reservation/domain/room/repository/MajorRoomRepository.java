package edu.skku.scg.reservation.domain.room.repository;

import edu.skku.scg.reservation.domain.room.entity.MajorRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MajorRoomRepository extends JpaRepository<MajorRoom, Long> {

    @Query("SELECT mr FROM MajorRoom mr " +
            "JOIN FETCH mr.major " +
            "WHERE mr.room.id IN :roomIds")
    List<MajorRoom> findByRoomIdsWithMajors(List<Long> roomIds);
}