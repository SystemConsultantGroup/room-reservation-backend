package edu.skku.scg.reservation.domain.organization.service;

import edu.skku.scg.reservation.domain.organization.repository.OriginManagementUnitRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OriginService {

    private final OriginManagementUnitRepository originManagementUnitRepository;

    @Cacheable(cacheNames = "originManagementUnitId", key = "#originUrl")
    public Long getManagementUnitId(String originUrl) {
        return originManagementUnitRepository.findByOriginUrl(originUrl).
                orElseThrow(() -> new BusinessException(ErrorCode.UNREGISTERED_ORIGIN))
                .getManagementUnit().getId();
    }
}
