package edu.skku.scg.reservation.domain.room.service;

import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.user.entity.MajorType;
import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomAccessChecker {

    public void checkAccess(User user, Room room) {
        if (user.getType() == UserType.GUEST) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (!hasOverlappingMajor(user, room)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        switch (room.getAccessPolicy()) {
            case ALL:
                break;
            case ONLY_FIRST_MAJOR:
                if (user.getType() == UserType.STUDENT && !hasOverlappingPrimaryMajor(user, room)) {
                    throw new BusinessException(ErrorCode.ACCESS_DENIED);
                }
                break;
            case ONLY_FACULTY:
                if (user.getType() != UserType.FACULTY) {
                    throw new BusinessException(ErrorCode.ACCESS_DENIED);
                }
                break;
        }
    }

    private boolean hasOverlappingMajor(User user, Room room) {
        Set<Long> userMajorIds = user.getUserMajors().stream()
                .filter(userMajor -> userMajor.getStatus() == RegistrationStatus.APPROVED)
                .map(userMajor -> userMajor.getMajor().getId())
                .collect(Collectors.toSet());

        return room.getMajorRooms().stream()
                .map(roomMajor -> roomMajor.getMajor().getId())
                .anyMatch(userMajorIds::contains);
    }

    private boolean hasOverlappingPrimaryMajor(User user, Room room) {
        Set<Long> roomMajorIds = room.getMajorRooms().stream()
                .map(roomMajor -> roomMajor.getMajor().getId())
                .collect(Collectors.toSet());

        return user.getUserMajors().stream()
                .filter(userMajor -> userMajor.getStatus() == RegistrationStatus.APPROVED)
                .filter(userMajor -> userMajor.getType() == MajorType.FIRST)
                .map(userMajor -> userMajor.getMajor().getId())
                .anyMatch(roomMajorIds::contains);
    }
}
