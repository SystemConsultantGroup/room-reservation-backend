package edu.skku.scg.reservation.domain.user.service;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.user.dto.GetMeResponse;
import edu.skku.scg.reservation.domain.user.dto.UpdateMeRequest;
import edu.skku.scg.reservation.domain.user.dto.UserDetail;
import edu.skku.scg.reservation.domain.user.dto.UserInfo;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.reservation.service.ReservationService;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ReservationService reservationService;

    public GetMeResponse getUserDetail(UserPrincipal principal) {
        User user = userRepository.findByIdWithMajors(principal.getId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return GetMeResponse.from(user, principal.getManagingUnitIds());
    }

    public Page<UserInfo> getUsers(List<Long> managementUnitIds, Pageable pageable, String keyword) {
        Page<User> users = userRepository
                .findUsersByUnitIds(
                        managementUnitIds,
                        pageable,
                        keyword);

        return users.map(UserInfo::from);
    }

    @Transactional
    public void updateMe(Long userId, UpdateMeRequest dto) {
        User user = getUserOrThrow(userId);
        user.updateName(dto.name());
    }

    public UserDetail getUserDetail(Long userId, List<Long> managingUnitIds) {
        User user = getUserWithMajorsOrThrow(userId);
        validateUserManagementAccess(user, managingUnitIds);
        return UserDetail.from(user);
    }

    @Transactional
    public void cancelAllFutureReservations(Long userId, List<Long> managingUnitIds) {
        User user = getUserWithMajorsOrThrow(userId);
        validateUserManagementAccess(user, managingUnitIds);
        reservationService.cancelAllFutureReservations(userId, managingUnitIds);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private User getUserWithMajorsOrThrow(Long userId) {
        return userRepository.findByIdWithMajors(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateUserManagementAccess(User user, List<Long> managingUnitIds) {
        boolean isManagedUser = user.getUserMajors().stream()
                .map(userMajor -> userMajor.getMajor().getManagementUnit().getId())
                .anyMatch(managingUnitIds::contains);

        if (!isManagedUser) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
