package edu.skku.scg.reservation.domain.room.service;

import edu.skku.scg.reservation.domain.organization.entity.Major;
import edu.skku.scg.reservation.domain.organization.repository.MajorRepository;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetail;
import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.reservation.repository.ReservationRepository;
import edu.skku.scg.reservation.domain.room.dto.*;
import edu.skku.scg.reservation.domain.room.entity.MajorRoom;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.entity.RoomOperatingHour;
import edu.skku.scg.reservation.domain.room.repository.RoomRepository;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final MajorRepository majorRepository;
    private final RoomAccessChecker roomAccessChecker;

    @Transactional
    public void createRoom(RoomCreateRequest dto, List<Long> managingUnitIds) {
        validateMajorsOwnership(dto.majorIds(), managingUnitIds);

        if (dto.minAttendeeCount() > dto.maxAttendeeCount()) {
            throw new BusinessException(ErrorCode.INVALID_ATTENDEE_COUNT_RANGE);
        }

        if (dto.minUsageMinutes() > dto.maxUsageMinutes()) {
            throw new BusinessException(ErrorCode.INVALID_USAGE_TIME_RANGE);
        }

        Room room = Room.builder()
                .name(dto.name())
                .minAttendeeCount(dto.minAttendeeCount())
                .maxAttendeeCount(dto.maxAttendeeCount())
                .roomNumber(dto.roomNumber())
                .accessPolicy(dto.accessPolicy())
                .minUsageMinutes(dto.minUsageMinutes())
                .maxUsageMinutes(dto.maxUsageMinutes())
                .build();

        mapMajorsToRoom(room, dto.majorIds());
        mapOperatingHoursToRoom(room, dto.operatingHours());

        roomRepository.save(room);
    }

    @Transactional
    public void updateRoom(Long roomId, RoomUpdateRequest dto, List<Long> managingUnitIds) {
        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        List<Long> currentMajorIds = room.getMajorRooms().stream()
                .map(mr -> mr.getMajor().getId())
                .toList();

        validateMajorsOwnership(currentMajorIds, managingUnitIds);
        validateMajorsOwnership(dto.majorIds(), managingUnitIds);

        room.update(dto);

        room.getMajorRooms().clear();
        mapMajorsToRoom(room, dto.majorIds());

        room.getOperatingHours().clear();
        mapOperatingHoursToRoom(room, dto.operatingHours());
    }

    @Transactional
    public void deleteRoom(Long roomId, List<Long> managingUnitIds) {
        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        List<Long> currentMajorIds = room.getMajorRooms().stream()
                .map(mr -> mr.getMajor().getId())
                .toList();
        validateMajorsOwnership(currentMajorIds, managingUnitIds);

        roomRepository.delete(room);
    }

    public RoomResponse getRoom(Long roomId) {
        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return convertToRoomResponse(room);
    }

    public Page<RoomInfo> getRooms(List<Long> managingUnitIds, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findRoomsByManagementUnitIds(managingUnitIds, pageable);
        return roomPage.map(RoomInfo::from);
    }

    public RoomSummaryList getRoomSummaries(Long managementUnitId, Long userId) {
        List<Room> rooms = roomRepository.findAllByManagementUnitIdWithMajors(managementUnitId);
        User user =
                userId == null ? null :
                userRepository.findByIdWithMajors(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return RoomSummaryList.from(rooms, room -> canUserReserveRoom(user, room));
    }

    public Page<DailyRoomScheduleResponse> getDailyRoomSchedules(Long managementUnitId, LocalDate date, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findRoomsByManagementUnitId(managementUnitId, pageable);

        if (roomPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> roomIds = roomPage.getContent().stream().map(Room::getId).toList();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Reservation> allReservations = reservationRepository
                .findReservationsByRoomIdsAndDate(roomIds, startOfDay, endOfDay);

        Map<Long, List<Reservation>> reservationMap = allReservations.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getRoom().getId()));

        return roomPage.map(room -> {
            Long roomId = room.getId();

            List<Reservation> roomReservations = reservationMap.getOrDefault(roomId, List.of());

            RoomOperatingHour todayHour = room.getOperatingHours().stream()
                    .filter(hour -> hour.getDayOfWeek().equals(date.getDayOfWeek()))
                    .findFirst()
                    .orElse(null);

            return DailyRoomScheduleResponse.from(room, todayHour, roomReservations);
        });
    }

    public WeeklyRoomScheduleResponse getWeeklyRoomSchedules(LocalDate date, Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }

        int daysFromSunday = date.getDayOfWeek().getValue() % 7;

        LocalDate startOfWeek = date.minusDays(daysFromSunday);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.plusDays(1).atStartOfDay();

        List<Reservation> weeklyReservations = reservationRepository
                .findReservationsByRoomIdAndDate(roomId, startDateTime, endDateTime);

        return WeeklyRoomScheduleResponse.from(roomId, weeklyReservations);
    }

    public Page<ReservationDetail> getFutureReservations(
            Long roomId,
            List<Long> managingUnitIds,
            Pageable pageable) {

        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        validateRoomOwnership(room, managingUnitIds);

        Page<Reservation> reservations = reservationRepository.findFutureReservationsByRoomId(
                roomId,
                LocalDateTime.now(),
                pageable
        );

        return reservations.map(ReservationDetail::from);
    }

    private void validateMajorsOwnership(List<Long> majorIds, List<Long> managingUnitIds) {
        if (majorIds == null || majorIds.isEmpty()) return;

        majorIds = majorIds.stream().distinct().toList();
        List<Major> majors = majorRepository.findAllById(majorIds);

        if (majors.size() != majorIds.size()) {
            throw new BusinessException(ErrorCode.MAJOR_NOT_FOUND);
        }

        for (Major major : majors) {
            Long unitId = major.getManagementUnit().getId();
            if (!managingUnitIds.contains(unitId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    private void mapMajorsToRoom(Room room, List<Long> majorIds) {
        if (majorIds == null || majorIds.isEmpty()) return;

        List<Major> majors = majorRepository.findAllById(majorIds);

        if (majors.size() != majorIds.size()) {
            throw new BusinessException(ErrorCode.MAJOR_NOT_FOUND);
        }

        for (Major major : majors) {
            MajorRoom majorRoom = MajorRoom.builder()
                    .major(major)
                    .room(room)
                    .build();
            room.getMajorRooms().add(majorRoom);
        }
    }

    private void mapOperatingHoursToRoom(Room room, List<OperatingHoursDetail> dtos) {
        long distinctCount = dtos.stream()
                .map(OperatingHoursDetail::dayOfWeek)
                .distinct()
                .count();

        if (distinctCount != dtos.size()) {
            throw new BusinessException(ErrorCode.DUPLICATE_DAY_OF_WEEK);
        }

        List<RoomOperatingHour> operatingHours = dtos.stream()
                .map(dto -> {
                    if (!dto.openTime().isBefore(dto.closeTime())) {
                        throw new BusinessException(ErrorCode.INVALID_TIME_ORDER);
                    }

                    return RoomOperatingHour.builder()
                            .dayOfWeek(dto.dayOfWeek())
                            .openTime(dto.openTime())
                            .closeTime(dto.closeTime())
                            .room(room)
                            .build();
                }).toList();

        roomRepository.flush();
        room.getOperatingHours().addAll(operatingHours);
    }

    private RoomResponse convertToRoomResponse(Room room) {
        return RoomResponse.from(room);
    }

    private boolean canUserReserveRoom(User user, Room room) {
        try {
            if (user != null) {
                roomAccessChecker.checkAccess(user, room);
                return true;
            }
        } catch (BusinessException ignored) {
            return false;
        }

        return false;
    }

    public void validateRoomOwnership(Room room, List<Long> managingUnitIds) {
        boolean hasUnmanagedMajor = room.getMajorRooms().stream()
                .map(majorRoom -> majorRoom.getMajor().getManagementUnit().getId())
                .anyMatch(unitId -> !managingUnitIds.contains(unitId));

        if (hasUnmanagedMajor) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
