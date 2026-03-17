package edu.skku.scg.reservation.domain.auth.common;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import org.springframework.stereotype.Component;

@Component("authChecker")
public class AuthChecker {

    public boolean isApproved(UserPrincipal principal, Long cid) {
        if (principal == null) return false;
        return principal.getApprovedCids().contains(cid);
    }

    public boolean isAdmin(UserPrincipal principal, Long cid) {
        if (principal == null) return false;
        boolean isCollegeAdmin = principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("COLLEGE_ADMIN"));
        if (!isCollegeAdmin) return false;
        return principal.getAdminCids().contains(cid);
    }

    public boolean isSuperAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPER_ADMIN"));
    }
}