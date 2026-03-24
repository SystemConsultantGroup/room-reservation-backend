package edu.skku.scg.reservation.domain.reservation.service;

import edu.skku.scg.reservation.domain.reservation.dto.CreateReservationRequestDto;
import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.reservation.repository.ReservationRepository;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.entity.RoomOperatingHour;
import edu.skku.scg.reservation.domain.room.repository.RoomOperatingHourRepository;
import edu.skku.scg.reservation.domain.room.repository.RoomRepository;
import edu.skku.scg.reservation.domain.room.service.RoomAccessChecker;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final RoomRepository roomRepository;
    private final RoomOperatingHourRepository roomOperatingHourRepository;
    private final ReservationRepository reservationRepository;
    private final RoomAccessChecker roomAccessChecker;
    private final UserRepository userRepository;

    @Transactional
    public void reserveRoom(Long userId, CreateReservationRequestDto dto) {
        User user = userRepository.findByIdWithMajors(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Room room = roomRepository.findByIdWithMajorsAndLock(dto.roomId()).orElseThrow(
                () -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        roomAccessChecker.checkAccess(user, room);

        validateTimeRange(dto.startTime(), dto.endTime());

        RoomOperatingHour roomOperatingHour = roomOperatingHourRepository
                .findByRoomIdAndDayOfWeek(room.getId(), dto.startTime().getDayOfWeek())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_AVAILABLE_TIME));

        validateOperatingHours(dto.startTime(), dto.endTime(), roomOperatingHour);
        validateMaxBookingTime(dto.startTime(), dto.endTime(), room.getMaxBookingMinutes());
        validateReservationConflict(dto.startTime(), dto.endTime(), room.getId());

        Reservation reservation = Reservation.builder()
                .user(user)
                .room(room)
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .attendeeCount(dto.attendeeCount())
                .build();

        reservationRepository.save(reservation);
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE);
        }

        if (!startTime.toLocalDate().isEqual(endTime.toLocalDate())) {
            throw new BusinessException(ErrorCode.DATE_MISMATCH);
        }
    }

    private void validateOperatingHours(LocalDateTime startTime, LocalDateTime endTime, RoomOperatingHour operatingHour) {
        LocalTime reqStartTime = startTime.toLocalTime();
        LocalTime reqEndTime = endTime.toLocalTime();

        if (reqStartTime.isBefore(operatingHour.getOpenTime()) || reqEndTime.isAfter(operatingHour.getCloseTime())) {
            throw new BusinessException(ErrorCode.NOT_AVAILABLE_TIME);
        }
    }

    private void validateMaxBookingTime(LocalDateTime startTime, LocalDateTime endTime, Integer maxBookingMinutes) {
        if (maxBookingMinutes == null) return;

        long requestedMinutes = Duration.between(startTime, endTime).toMinutes();
        if (requestedMinutes > maxBookingMinutes) {
            throw new BusinessException(ErrorCode.EXCEED_MAX_BOOKING_TIME);
        }
    }

    private void validateReservationConflict(LocalDateTime startTime, LocalDateTime endTime, Long roomId) {
        if (reservationRepository.existsOverlappingReservation(roomId, startTime, endTime)) {
            throw new BusinessException(ErrorCode.NOT_AVAILABLE_TIME);
        }
    }
}