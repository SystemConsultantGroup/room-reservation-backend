package edu.skku.scg.reservation.domain.user.service;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.user.dto.MajorInfo;
import edu.skku.scg.reservation.domain.user.dto.UserDetailDto;
import edu.skku.scg.reservation.domain.user.dto.UserInfo;
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

    public UserDetailDto getUserDetail(UserPrincipal principal) {
        User user = userRepository.findByIdWithMajors(principal.getId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<MajorInfo> majors = user.getUserMajors().stream()
                .map(userMajor -> MajorInfo.builder()
                        .id(userMajor.getMajor().getId())
                        .name(userMajor.getMajor().getName())
                        .type(userMajor.getType())
                        .build()
                ).toList();

        return UserDetailDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .studentId(user.getStudentId())
                .type(user.getType())
                .managingUnitIds(principal.getManagingUnitIds())
                .build();
    }

    public Page<UserInfo> getUsers(List<Long> managementUnitIds, Pageable pageable, String keyword) {
        Page<User> users = userRepository.findUsersByUnitIds(managementUnitIds, pageable, keyword);
        return users.map(user -> UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .type(user.getType())
                .majors(
                        user.getUserMajors().stream()
                                .map(userMajor -> MajorInfo.builder()
                                        .id(userMajor.getMajor().getId())
                                        .name(userMajor.getMajor().getName())
                                        .type(userMajor.getType())
                                        .build()
                                ).toList()
                )
                .build());
    }
}
