package edu.skku.scg.reservation.domain.auth.common;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("authChecker")
@RequiredArgsConstructor
public class AuthChecker {

    public boolean isAdmin(UserPrincipal principal, Long managementUnitId) {
        if (principal == null) {
            return false;
        }
        return principal.getManagingUnitIds().contains(managementUnitId);
    }
}