package edu.skku.scg.reservation.domain.reservation.service;

import edu.skku.scg.reservation.domain.reservation.dto.CreateReservationRequest;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetail;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationList;
import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.reservation.repository.ReservationRepository;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.entity.RoomOperatingHour;
import edu.skku.scg.reservation.domain.room.repository.RoomOperatingHourRepository;
import edu.skku.scg.reservation.domain.room.repository.RoomRepository;
import edu.skku.scg.reservation.domain.room.service.RoomAccessChecker;
import edu.skku.scg.reservation.domain.user.dto.UserSummary;
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
import java.util.List;

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
    public void reserveRoom(Long userId, CreateReservationRequest dto) {
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
        validateUsageTime(dto.startTime(), dto.endTime(), room.getMinUsageMinutes(), room.getMaxUsageMinutes());
        validateAttendeeCount(dto.attendeeCount(), room.getMinAttendeeCount(), room.getMaxAttendeeCount());
        validateReservationConflict(dto.startTime(), dto.endTime(), room.getId());

        Reservation reservation = Reservation.builder()
                .user(user)
                .room(room)
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .attendeeCount(dto.attendeeCount())
                .purpose(dto.purpose())
                .build();

        reservationRepository.save(reservation);
    }

    public ReservationList getMyReservations(Long userId, LocalDateTime standardTime) {
        List<Reservation> reservations = reservationRepository
                .findReservationsByUserIdAndTimeAfter(userId, standardTime);

        List<ReservationDetail> details = reservations.stream()
                .map(r -> ReservationDetail.builder()
                        .id(r.getId())
                        .user(new UserSummary(r.getUser().getId(), r.getUser().getName()))
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .attendeeCount(r.getAttendeeCount())
                        .purpose(r.getPurpose())
                        .build())
                .toList();

        return ReservationList.builder()
                .reservations(details)
                .build();
    }

    @Transactional
    public void deleteReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (reservation.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_PAST_RESERVATION);
        }

        reservationRepository.delete(reservation);
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PAST_TIME_NOT_ALLOWED);
        }

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

    private void validateUsageTime(LocalDateTime startTime, LocalDateTime endTime, Integer minUsageMinutes, Integer maxUsageMinutes) {
        long requestedMinutes = Duration.between(startTime, endTime).toMinutes();

        if (requestedMinutes > maxUsageMinutes) {
            throw new BusinessException(ErrorCode.EXCEED_MAX_USAGE_TIME);
        }

        if (requestedMinutes < minUsageMinutes) {
            throw new BusinessException(ErrorCode.UNDER_MIN_USAGE_TIME);
        }
    }

    private void validateAttendeeCount(Integer attendeeCount, Integer minAttendeeCount, Integer maxAttendeeCount) {
        if (attendeeCount < minAttendeeCount) {
            throw new BusinessException(ErrorCode.UNDER_MIN_ATTENDEE_COUNT);
        }

        if (attendeeCount > maxAttendeeCount) {
            throw new BusinessException(ErrorCode.EXCEED_MAX_ATTENDEE_COUNT);
        }
    }

    private void validateReservationConflict(LocalDateTime startTime, LocalDateTime endTime, Long roomId) {
        if (reservationRepository.existsOverlappingReservation(roomId, startTime, endTime)) {
            throw new BusinessException(ErrorCode.NOT_AVAILABLE_TIME);
        }
    }
}