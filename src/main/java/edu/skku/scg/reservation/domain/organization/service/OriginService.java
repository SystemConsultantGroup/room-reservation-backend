package edu.skku.scg.reservation.domain.organization.service;

import edu.skku.scg.reservation.domain.organization.repository.OriginManagementUnitRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OriginService {

    private final OriginManagementUnitRepository originManagementUnitRepository;

    public Long getManagementUnitId(String originUrl) {
        return originManagementUnitRepository.findManagementUnitIdByOriginUrl(originUrl).
                orElseThrow(() -> new BusinessException(ErrorCode.UNREGISTERED_ORIGIN));
    }

    public void validateOriginUrl(String originUrl) {
        this.getManagementUnitId(originUrl);
    }
}
