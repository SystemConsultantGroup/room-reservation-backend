package edu.skku.scg.reservation.domain.organization.service;

import edu.skku.scg.reservation.domain.organization.dto.ManagementUnitDetail;
import edu.skku.scg.reservation.domain.organization.dto.NoticeDetail;
import edu.skku.scg.reservation.domain.organization.dto.UpdateNoticeRequest;
import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;
import edu.skku.scg.reservation.domain.organization.repository.ManagementUnitRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagementUnitService {

    private final ManagementUnitRepository managementUnitRepository;

    public ManagementUnitDetail getManagementUnit(Long managementUnitId) {
        ManagementUnit managementUnit = getManagementUnitOrThrow(managementUnitId);
        return ManagementUnitDetail.builder()
                .id(managementUnit.getId())
                .name(managementUnit.getName())
                .approvalMethod(managementUnit.getApprovalMethod())
                .noticeTitle(managementUnit.getNoticeTitle())
                .noticeContent(managementUnit.getNoticeContent())
                .build();
    }

    @Transactional
    public void updateNotice(
            Long managementUnitId,
            UpdateNoticeRequest dto,
            List<Long> managingUnitIds) {

        if (!managingUnitIds.contains(managementUnitId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        ManagementUnit managementUnit = getManagementUnitOrThrow(managementUnitId);
        managementUnit.updateNotice(dto.title(), dto.content());
    }

    private ManagementUnit getManagementUnitOrThrow(Long managementUnitId) {
        return managementUnitRepository.findById(managementUnitId).orElseThrow(
                () -> new BusinessException(ErrorCode.MANAGEMENT_UNIT_NOT_FOUND)
        );
    }
}
