package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.auth.AuthRequest;
import com.engineering.orgcore.dto.auth.AuthResponse;
import com.engineering.orgcore.dto.auth.LoginRequest;
import com.engineering.orgcore.service.AuthenticationService;
import com.engineering.orgcore.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final Utils utils;
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
