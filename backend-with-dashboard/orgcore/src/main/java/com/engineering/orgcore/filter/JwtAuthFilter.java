package com.engineering.orgcore.filter;

import com.engineering.orgcore.config.UserDetailsImpl;
import com.engineering.orgcore.dto.auth.AuthPrincipal;
import com.engineering.orgcore.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // your util that validates/parses token
    private final UserDetailsImpl userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsImpl uds) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = uds;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if ( auth != null && auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtUtil.isTokenValid(token)) {

                String email = jwtUtil.extractEmail(token);
                Long tenantId = jwtUtil.extractTenantId(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                AuthPrincipal principal = new AuthPrincipal(
                        userDetails.getUsername(),
                        tenantId
                );
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
