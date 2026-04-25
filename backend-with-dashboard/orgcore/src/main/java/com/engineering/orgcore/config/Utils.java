package com.engineering.orgcore.config;

import com.engineering.orgcore.dto.auth.AuthPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Utils {

    public String getCurrentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthPrincipal p) return p.email();

        return auth.getName(); // fallback
    }

    public Long getCurrentTenant() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthPrincipal p) return p.tenantId();

        return null;
    }
}
