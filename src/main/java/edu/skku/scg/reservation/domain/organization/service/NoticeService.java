package edu.skku.scg.reservation.domain.organization.service;

import edu.skku.scg.reservation.domain.organization.dto.NoticeDetailDto;
import edu.skku.scg.reservation.domain.organization.dto.UpdateNoticeRequestDto;
import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;
import edu.skku.scg.reservation.domain.organization.repository.ManagementUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void updateNotice(Long managementUnitId, UpdateNoticeRequestDto dto) {
        ManagementUnit managementUnit = managementUnitRepository.findById(managementUnitId).orElseThrow(
                () -> new IllegalStateException("Management unit not found")
        );

        managementUnit.updateNotice(dto.title(), dto.content());
    }
}
