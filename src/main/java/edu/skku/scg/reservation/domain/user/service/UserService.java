package edu.skku.scg.reservation.domain.user.service;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.user.dto.GetMeResponse;
import edu.skku.scg.reservation.domain.user.dto.UserInfo;
import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import edu.skku.scg.reservation.domain.user.entity.User;
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

    public GetMeResponse getUserDetail(UserPrincipal principal) {
        User user = userRepository.findByIdWithMajors(principal.getId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return GetMeResponse.from(user, principal.getManagingUnitIds());
    }

    public Page<UserInfo> getUsers(List<Long> managementUnitIds, Pageable pageable, String keyword) {
        Page<User> users = userRepository
                .findUsersByUnitIdsAndRegistrationStatus(
                        managementUnitIds,
                        RegistrationStatus.APPROVED,
                        pageable,
                        keyword);

        return users.map(UserInfo::from);
    }
}
