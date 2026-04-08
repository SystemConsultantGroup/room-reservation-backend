package edu.skku.scg.reservation.domain.organization.service;

import edu.skku.scg.reservation.domain.organization.dto.*;
import edu.skku.scg.reservation.domain.organization.entity.Major;
import edu.skku.scg.reservation.domain.organization.repository.MajorRepository;
import edu.skku.scg.reservation.domain.user.entity.*;
import edu.skku.scg.reservation.domain.user.repository.UserMajorRepository;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MajorService {

    private final MajorRepository majorRepository;
    private final UserMajorRepository userMajorRepository;
    private final UserRepository userRepository;

    @Transactional
    public void applyMajor(Long userId, List<MajorRequest> majorRequests) {
        List<Long> majorIds = majorRequests.stream()
                .map(MajorRequest::id)
                .distinct()
                .toList();

        if (majorIds.size() != majorRequests.size()) {
            throw new BusinessException(ErrorCode.DUPLICATE_MAJOR_REQUEST);
        }

        List<Major> majors = majorRepository.findAllById(majorIds);
        if (majors.size() != majorIds.size()) {
            throw new BusinessException(ErrorCode.MAJOR_NOT_FOUND);
        }

        User user = userRepository.findByIdWithMajors(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<MajorType> approvedTypes = user.getUserMajors().stream()
                .filter(um -> um.getStatus() == RegistrationStatus.APPROVED)
                .map(UserMajor::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<MajorType> pendingTypes = user.getUserMajors().stream()
                .filter(um -> um.getStatus() == RegistrationStatus.PENDING)
                .map(UserMajor::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<MajorType> applyingTypes = new HashSet<>();

        for (MajorRequest majorRequest : majorRequests) {
            validateUserTypeRequirement(user, majorRequest);

            boolean alreadyApplied = user.getUserMajors().stream()
                    .anyMatch(um -> um.getMajor().getId().equals(majorRequest.id()) &&
                            um.getStatus() != RegistrationStatus.REJECTED);
            if (alreadyApplied) {
                throw new BusinessException(ErrorCode.MAJOR_ALREADY_APPLIED);
            }

            if (majorRequest.type() != null) {
                if (approvedTypes.contains(majorRequest.type())) {
                    throw new BusinessException(ErrorCode.ALREADY_HELD_MAJOR_TYPE);
                }

                if (pendingTypes.contains(majorRequest.type()) || applyingTypes.contains(majorRequest.type())) {
                    throw new BusinessException(ErrorCode.DUPLICATE_MAJOR_TYPE_REQUEST);
                }

                applyingTypes.add(majorRequest.type());
            }

            Major major = majorRepository.getReferenceById(majorRequest.id());

            UserMajor userMajor = UserMajor.builder()
                    .user(user)
                    .major(major)
                    .type(majorRequest.type())
                    .build();

            userMajorRepository.save(userMajor);
        }
    }

    @Transactional
    public void approveApplication(Long userMajorId, List<Long> managingUnitIds) {
        UserMajor userMajor = getUserMajor(userMajorId);

        validateMajorOwnership(managingUnitIds, userMajor);

        if (userMajor.getStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_MAJOR_REGISTRATION);
        }

        userMajor.approve();
    }

    @Transactional
    public void rejectApplication(Long userMajorId, List<Long> managingUnitIds) {
        UserMajor userMajor = getUserMajor(userMajorId);

        validateMajorOwnership(managingUnitIds, userMajor);

        if (userMajor.getStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_MAJOR_REGISTRATION);
        }

        userMajor.reject();
    }

    public List<MajorSummary> getMajorSummaries(Long managementUnitId) {
        List<Major> majors = majorRepository.findAllByManagementUnitId(managementUnitId);
        return majors.stream()
                .map(MajorSummary::from)
                .toList();
    }

    public List<MajorSummary> getMajorSummaries(List<Long> managementUnitIds) {
        List<Major> majors = majorRepository.findAllByManagementUnitIdIn(managementUnitIds);
        return majors.stream()
                .map(MajorSummary::from)
                .toList();
    }

    public Page<MajorApplicationDetail> getApplications(
            List<Long> managingUnitIds,
            Pageable pageable,
            String keyword) {

        if (!StringUtils.hasText(keyword)) {
            keyword = null;
        }

        Page<User> users = userRepository.
                findUsersByUnitIdsAndRegistrationStatus(
                managingUnitIds,
                RegistrationStatus.PENDING,
                pageable,
                keyword);

        return users.map(user -> MajorApplicationDetail.from(user, managingUnitIds));
    }

    public MajorApplicationList getApplications(Long userId) {
        User user = userRepository.findByIdWithMajors(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MajorApplicationList.from(user);
    }

    private UserMajor getUserMajor(Long userMajorId) {
        return userMajorRepository.findById(userMajorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_MAJOR_NOT_FOUND));
    }

    @Transactional
    public void cancelApplication(Long userId, Long userMajorId) {
        UserMajor userMajor = getUserMajor(userMajorId);

        if (!userMajor.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (userMajor.getStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_MAJOR_REGISTRATION);
        }

        userMajorRepository.delete(userMajor);
    }

    private void validateMajorOwnership(List<Long> managingUnitIds, UserMajor userMajor) {
        if (!managingUnitIds.contains(userMajor.getMajor().getManagementUnit().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateUserTypeRequirement(User user, MajorRequest request) {
        if (user.getType() == UserType.STUDENT && request.type() == null) {
            throw new BusinessException(ErrorCode.INVALID_STUDENT_MAJOR_TYPE);
        }
        if (user.getType() == UserType.FACULTY && request.type() != null) {
            throw new BusinessException(ErrorCode.INVALID_FACULTY_MAJOR_TYPE);
        }
    }
}
