package edu.skku.scg.reservation.domain.auth.common;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.reservation.entity.Room;
import edu.skku.scg.reservation.domain.reservation.repository.RoomRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("authChecker")
@RequiredArgsConstructor
public class AuthChecker {

    private final RoomRepository roomRepository;

    public boolean isApproved(UserPrincipal principal, Long roomId) {
        if (principal == null) return false;
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return principal.getApprovedCids().contains(room.getCollege().getId());
    }

    public boolean isAdmin(UserPrincipal principal, Long collegeId) {
        if (principal == null) return false;
        boolean isCollegeAdmin = principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("COLLEGE_ADMIN"));
        if (!isCollegeAdmin) return false;
        return principal.getAdminCids().contains(collegeId);
    }

    public boolean isSuperAdmin(UserPrincipal principal) {
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPER_ADMIN"));
    }
}