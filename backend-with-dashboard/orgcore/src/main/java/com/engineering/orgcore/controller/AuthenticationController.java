package com.engineering.orgcore.controller;

import com.engineering.orgcore.dto.auth.AuthRequest;
import com.engineering.orgcore.dto.auth.AuthResponse;
import com.engineering.orgcore.dto.auth.TenantDto;
import com.engineering.orgcore.entity.Tenant;
import com.engineering.orgcore.entity.Users;
import com.engineering.orgcore.repository.TenantRepository;
import com.engineering.orgcore.repository.UsersRepository;
import com.engineering.orgcore.service.AuthenticationService;
import com.engineering.orgcore.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final UsersRepository usersRepository;
    private final TenantRepository tenantRepository;

    private final AuthenticationService authenticationService;



    private JwtUtil jwtUtil;



    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody AuthRequest request) {
return authenticationService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody Map<String, String> body) {
return  authenticationService.login(body);
    }

}
