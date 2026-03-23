package edu.skku.scg.reservation.domain.room.service;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummaryDto;
import edu.skku.scg.reservation.domain.organization.entity.Major;
import edu.skku.scg.reservation.domain.organization.repository.MajorRepository;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetailDto;
import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.reservation.repository.ReservationRepository;
import edu.skku.scg.reservation.domain.room.dto.RoomCreateRequestDto;
import edu.skku.scg.reservation.domain.room.dto.RoomDetailDto;
import edu.skku.scg.reservation.domain.room.dto.RoomScheduleResponseDto;
import edu.skku.scg.reservation.domain.room.dto.RoomUpdateRequestDto;
import edu.skku.scg.reservation.domain.room.entity.MajorRoom;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.repository.RoomRepository;
import edu.skku.scg.reservation.domain.user.dto.UserSummaryDto;
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
    private final ReservationRepository reservationRepository;
    private final MajorRepository majorRepository;

    @Transactional
    public RoomDetailDto createRoom(RoomCreateRequestDto dto, List<Long> managingUnitIds) {
        validateMajorsOwnership(dto.majorIds(), managingUnitIds);

        Room room = Room.builder()
                .name(dto.name())
                .capacity(dto.capacity())
                .roomNumber(dto.roomNumber())
                .accessPolicy(dto.accessPolicy())
                .build();

        mapMajorsToRoom(room, dto.majorIds());

        Room savedRoom = roomRepository.save(room);
        return convertToRoomDetailDto(savedRoom);
    }

    public RoomDetailDto getRoom(Long roomId) {
        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return convertToRoomDetailDto(room);
    }

    @Transactional
    public RoomDetailDto updateRoom(Long roomId, RoomUpdateRequestDto dto, List<Long> managingUnitIds) {
        Room room = roomRepository.findByIdWithMajors(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        List<Long> currentMajorIds = room.getMajorRooms().stream()
                .map(mr -> mr.getMajor().getId())
                .toList();

        validateMajorsOwnership(currentMajorIds, managingUnitIds);
        validateMajorsOwnership(dto.majorIds(), managingUnitIds);

        room.update(dto.name(), dto.capacity(), dto.roomNumber(), dto.accessPolicy());

        room.getMajorRooms().clear();
        mapMajorsToRoom(room, dto.majorIds());

        return convertToRoomDetailDto(room);
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

    public Page<RoomScheduleResponseDto> getDailyRoomSchedules(Long managementUnitId, LocalDate date, Pageable pageable) {
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

        return assembleRoomAndReservation(roomPage, allReservations);
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

    private RoomDetailDto convertToRoomDetailDto(Room room) {
        List<MajorSummaryDto> majors = room.getMajorRooms().stream()
                .map(majorRoom -> MajorSummaryDto.builder()
                        .id(majorRoom.getMajor().getId())
                        .name(majorRoom.getMajor().getName())
                        .build()
                ).toList();

        return RoomDetailDto.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .accessPolicy(room.getAccessPolicy())
                .majors(majors)
                .build();
    }

    private Page<RoomScheduleResponseDto> assembleRoomAndReservation(Page<Room> roomPage, List<Reservation> allReservations) {
        Map<Long, List<Reservation>> reservationMap = allReservations.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getRoom().getId()));

        return roomPage.map(room -> {
            List<MajorSummaryDto> majors = room.getMajorRooms().stream()
                    .map(majorRoom -> MajorSummaryDto.builder()
                            .name(majorRoom.getMajor().getName())
                            .build()
                    ).toList();

            List<Reservation> myReservations = reservationMap.getOrDefault(room.getId(), List.of());

            List<ReservationDetailDto> reservations = myReservations.stream()
                    .map(res -> ReservationDetailDto.builder()
                            .id(res.getId())
                            .startTime(res.getStartTime())
                            .endTime(res.getEndTime())
                            .user(new UserSummaryDto(res.getUser().getId(), res.getUser().getName()))
                            .build()
                    ).toList();

            return RoomScheduleResponseDto.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .capacity(room.getCapacity())
                    .accessPolicy(room.getAccessPolicy())
                    .majors(majors)
                    .reservations(reservations)
                    .build();
        });
    }
}