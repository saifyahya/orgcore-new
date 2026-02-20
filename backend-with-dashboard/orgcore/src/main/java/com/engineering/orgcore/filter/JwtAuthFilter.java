package com.engineering.orgcore.filter;

import com.engineering.orgcore.dto.auth.AuthPrincipal;
import com.engineering.orgcore.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.annotations.Filter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // your util that validates/parses token
    // Optional if you want to load user from DB:
    // private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil /*, UserDetailsService uds */) {
        this.jwtUtil = jwtUtil;
        // this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Expect: Authorization: Bearer <token>
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // If already authenticated, skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate token (signature + expiration)
            if (!jwtUtil.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract subject (usually email/username)
            String username = jwtUtil.extractUsername(token);
            Long tenantId = jwtUtil.extractTenantId(token);
            // Option A (simple): build authorities from token claims (if stored there)
            // Example: roles claim ["ROLE_ADMIN","ROLE_USER"] or similar
            List<SimpleGrantedAuthority> authorities = Collections.EMPTY_LIST;

            AuthPrincipal principal = new AuthPrincipal(username, tenantId);

            // Build UserDetails without hitting DB (if token is trusted and has roles)
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(username)
                    .password("") // not needed here
                    .authorities(authorities)
                    .build();

            // Create Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(tenantId);


            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // ✅ Set SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            // If anything fails, do not authenticate
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
