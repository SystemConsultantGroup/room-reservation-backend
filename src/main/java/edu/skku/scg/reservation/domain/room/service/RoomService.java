package edu.skku.scg.reservation.domain.room.service;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummaryDto;
import edu.skku.scg.reservation.domain.organization.entity.Major;
import edu.skku.scg.reservation.domain.organization.repository.MajorRepository;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetailDto;
import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.reservation.repository.ReservationRepository;
import edu.skku.scg.reservation.domain.room.dto.*;
import edu.skku.scg.reservation.domain.room.entity.MajorRoom;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.entity.RoomOperatingHour;
import edu.skku.scg.reservation.domain.room.repository.MajorRoomRepository;
import edu.skku.scg.reservation.domain.room.repository.RoomOperatingHourRepository;
import edu.skku.scg.reservation.domain.room.repository.RoomRepository;
import edu.skku.scg.reservation.domain.user.dto.UserSummaryDto;
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
import java.time.LocalTime;
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
    private final RoomOperatingHourRepository roomOperatingHourRepository;
    private final MajorRoomRepository majorRoomRepository;
    private final RoomAccessChecker roomAccessChecker;

    @Transactional
    public void createRoom(RoomCreateRequestDto dto, List<Long> managingUnitIds) {
        validateMajorsOwnership(dto.majorIds(), managingUnitIds);

        Room room = Room.builder()
                .name(dto.name())
                .capacity(dto.capacity())
                .roomNumber(dto.roomNumber())
                .accessPolicy(dto.accessPolicy())
                .maxBookingMinutes(dto.maxBookingMinutes())
                .build();

        mapMajorsToRoom(room, dto.majorIds());
        mapOperatingHoursToRoom(room, dto.operatingHours());

        roomRepository.save(room);
    }

    public RoomResponseDto getRoom(Long roomId, Long userId) {
        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userId == null ? null : userRepository.findByIdWithMajors(userId).orElse(null);

        return convertToRoomDetailDto(room, user);
    }

    @Transactional
    public void updateRoom(Long roomId, RoomUpdateRequestDto dto, List<Long> managingUnitIds) {
        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        List<Long> currentMajorIds = room.getMajorRooms().stream()
                .map(mr -> mr.getMajor().getId())
                .toList();

        validateMajorsOwnership(currentMajorIds, managingUnitIds);
        validateMajorsOwnership(dto.majorIds(), managingUnitIds);

        room.update(dto.name(), dto.capacity(), dto.roomNumber(), dto.accessPolicy(), dto.maxBookingMinutes());

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

    public Page<DailyRoomScheduleResponseDto> getDailyRoomSchedules(Long managementUnitId, LocalDate date, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findRoomsByManagementUnitId(managementUnitId, pageable);

        if (roomPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> roomIds = roomPage.getContent().stream()
                .map(Room::getId)
                .toList();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Reservation> allReservations = reservationRepository
                .findReservationsByRoomIdsAndDate(roomIds, startOfDay, endOfDay);

        List<RoomOperatingHour> operatingHours = roomOperatingHourRepository
                .findAllByRoomIdInAndDayOfWeek(roomIds, date.getDayOfWeek());

        return assembleDailyRoomSchedule(roomPage, allReservations, operatingHours);
    }

    public RoomSummaryListDto getRoomSummaries(Long managementUnitId) {
        List<Room> rooms = roomRepository.findAllByManagementUnitId(managementUnitId);

        List<RoomSummaryDto> dtos = rooms.stream().map(
                room -> RoomSummaryDto.builder()
                        .id(room.getId())
                        .name(room.getName())
                        .build()).toList();

        return RoomSummaryListDto.builder()
                .content(dtos)
                .build();
    }

    public Page<RoomInfoDto> getRooms(List<Long> managingUnitIds, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findRoomsByManagementUnitIds(managingUnitIds, pageable);

        if (roomPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> roomIds = roomPage.getContent().stream()
                .map(Room::getId)
                .toList();

        List<MajorRoom> allMajorRooms = majorRoomRepository.findByRoomIdsWithMajors(roomIds);

        Map<Long, List<MajorRoom>> majorRoomMap = allMajorRooms.stream()
                .collect(Collectors.groupingBy(mr -> mr.getRoom().getId()));

        return roomPage.map(room -> {
            List<MajorRoom> myMajorRooms = majorRoomMap.getOrDefault(room.getId(), List.of());

            List<MajorSummaryDto> majors = myMajorRooms.stream()
                    .map(mr -> MajorSummaryDto.builder()
                            .id(mr.getMajor().getId())
                            .name(mr.getMajor().getName())
                            .build()
                    ).toList();

            return RoomInfoDto.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .capacity(room.getCapacity())
                    .roomNumber(room.getRoomNumber())
                    .accessPolicy(room.getAccessPolicy())
                    .maxBookingMinutes(room.getMaxBookingMinutes())
                    .majors(majors)
                    .build();
        });
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

    private void mapOperatingHoursToRoom(Room room, List<OperatingHoursDetailDto> dtos) {
        List<RoomOperatingHour> operatingHours = dtos.stream()
                .map(dto -> RoomOperatingHour.builder()
                        .dayOfWeek(dto.dayOfWeek())
                        .openTime(dto.openTime())
                        .closeTime(dto.closeTime())
                        .room(room)
                        .build()
                ).toList();

        room.getOperatingHours().addAll(operatingHours);
    }

    private RoomResponseDto convertToRoomDetailDto(Room room, User user) {
        List<MajorSummaryDto> majors = room.getMajorRooms().stream()
                .map(majorRoom -> MajorSummaryDto.builder()
                        .id(majorRoom.getMajor().getId())
                        .name(majorRoom.getMajor().getName())
                        .build()
                ).toList();

        List<OperatingHoursDetailDto> operatingHours = room.getOperatingHours().stream()
                .map(operatingHour -> OperatingHoursDetailDto.builder()
                        .dayOfWeek(operatingHour.getDayOfWeek())
                        .openTime(operatingHour.getOpenTime())
                        .closeTime(operatingHour.getCloseTime())
                        .build()
                ).toList();

        boolean canReserve = canUserReserveRoom(user, room);

        return RoomResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .roomNumber(room.getRoomNumber())
                .accessPolicy(room.getAccessPolicy())
                .maxBookingMinutes(room.getMaxBookingMinutes())
                .majors(majors)
                .operatingHours(operatingHours)
                .canReserve(canReserve)
                .build();
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

    private Page<DailyRoomScheduleResponseDto> assembleDailyRoomSchedule(
            Page<Room> roomPage,
            List<Reservation> allReservations,
            List<RoomOperatingHour> operatingHours) {

        Map<Long, List<Reservation>> reservationMap = allReservations.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getRoom().getId()));

        Map<Long, List<RoomOperatingHour>> operatingHourMap = operatingHours.stream()
                .collect(Collectors.groupingBy(hour -> hour.getRoom().getId()));

        return roomPage.map(room -> {
            Long roomId = room.getId();

            List<MajorSummaryDto> majors = room.getMajorRooms().stream()
                    .map(majorRoom -> MajorSummaryDto.builder()
                            .id(majorRoom.getMajor().getId())
                            .name(majorRoom.getMajor().getName())
                            .build()
                    ).toList();

            List<Reservation> roomReservations = reservationMap.getOrDefault(roomId, List.of());
            List<ReservationDetailDto> reservations = roomReservations.stream()
                    .map(res -> ReservationDetailDto.builder()
                            .id(res.getId())
                            .startTime(res.getStartTime())
                            .endTime(res.getEndTime())
                            .user(new UserSummaryDto(res.getUser().getId(), res.getUser().getName()))
                            .attendeeCount(res.getAttendeeCount())
                            .purpose(res.getPurpose())
                            .build()
                    ).toList();

            List<RoomOperatingHour> myOperatingHours = operatingHourMap.getOrDefault(roomId, List.of());
            LocalTime openTime = null;
            LocalTime closeTime = null;

            if (!myOperatingHours.isEmpty()) {
                RoomOperatingHour todayHour = myOperatingHours.get(0);
                openTime = todayHour.getOpenTime();
                closeTime = todayHour.getCloseTime();
            }

            return DailyRoomScheduleResponseDto.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .capacity(room.getCapacity())
                    .accessPolicy(room.getAccessPolicy())
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .majors(majors)
                    .reservations(reservations)
                    .build();
        });
    }

    public WeeklyRoomScheduleResponseDto getWeeklyRoomSchedules(LocalDate date, Long roomId) {
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

        List<ReservationDetailDto> reservationDtos = weeklyReservations.stream()
                .map(res -> ReservationDetailDto.builder()
                        .id(res.getId())
                        .startTime(res.getStartTime())
                        .endTime(res.getEndTime())
                        .user(new UserSummaryDto(res.getUser().getId(), res.getUser().getName()))
                        .attendeeCount(res.getAttendeeCount())
                        .purpose(res.getPurpose())
                        .build()
                ).toList();

        return WeeklyRoomScheduleResponseDto.builder()
                .id(roomId)
                .reservations(reservationDtos)
                .build();
    }
}