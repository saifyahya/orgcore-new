package com.engineering.orgcore.controller;

import com.engineering.orgcore.dto.auth.AuthRequest;
import com.engineering.orgcore.dto.auth.AuthResponse;
import com.engineering.orgcore.dto.auth.LoginRequest;
import com.engineering.orgcore.exceptions.MissingDataException;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.TenantRepository;
import com.engineering.orgcore.repository.UsersRepository;
import com.engineering.orgcore.service.AuthenticationService;
import com.engineering.orgcore.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public AuthResponse signup(@RequestBody AuthRequest request) throws Exception {
        return authenticationService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest body) throws Exception {
        return authenticationService.login(body);
    }

}
