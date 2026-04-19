package com.engineering.orgcore.dto.auth;

public record LoginRequest(String email, String password, String customerName) {
}
