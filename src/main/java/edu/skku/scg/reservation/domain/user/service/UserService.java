package edu.skku.scg.reservation.domain.user.service;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.organization.entity.Major;
import edu.skku.scg.reservation.domain.organization.repository.MajorRepository;
import edu.skku.scg.reservation.domain.user.dto.UserDetailDto;
import edu.skku.scg.reservation.domain.user.entity.MajorType;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserMajor;
import edu.skku.scg.reservation.domain.user.repository.UserMajorRepository;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final UserMajorRepository userMajorRepository;

    public UserDetailDto getUserDetail(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserDetailDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .studentId(user.getStudentId())
                .type(user.getType())
                .managingUnitIds(principal.getManagingUnitIds())
                .build();
    }

    @Transactional
    public void applyMajor(Long userId, Long majorId, MajorType type) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Major major = majorRepository.findById(majorId).orElseThrow(() -> new BusinessException(ErrorCode.MAJOR_NOT_FOUND));

        UserMajor userMajor = UserMajor.builder()
                .user(user)
                .major(major)
                .type(type)
                .build();

        userMajorRepository.save(userMajor);
    }
}
