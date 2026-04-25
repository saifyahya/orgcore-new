package com.engineering.orgcore.dto.user;

public record AddUserRequest(String firstName, String lastName, String email, String password, Long branchId) {
}
