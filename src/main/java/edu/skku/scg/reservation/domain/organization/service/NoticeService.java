package edu.skku.scg.reservation.domain.organization.service;

import edu.skku.scg.reservation.domain.organization.dto.NoticeDetailDto;
import edu.skku.scg.reservation.domain.organization.dto.UpdateNoticeRequestDto;
import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;
import edu.skku.scg.reservation.domain.organization.repository.ManagementUnitRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final ManagementUnitRepository managementUnitRepository;

    public NoticeDetailDto getNotice(Long managementUnitId) {
        ManagementUnit managementUnit = managementUnitRepository.findById(managementUnitId).orElseThrow(
                () -> new IllegalStateException("Management unit not found")
        );

        return NoticeDetailDto.builder()
                .title(managementUnit.getNoticeTitle())
                .content(managementUnit.getNoticeContent())
                .build();
    }

    @Transactional
    public void updateNotice(
            Long managementUnitId,
            UpdateNoticeRequestDto dto,
            List<Long> managingUnitIds) {

        if (!managingUnitIds.contains(managementUnitId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        ManagementUnit managementUnit = managementUnitRepository.findById(managementUnitId).orElseThrow(
                () -> new IllegalStateException("Management unit not found")
        );

        managementUnit.updateNotice(dto.title(), dto.content());
    }
}
