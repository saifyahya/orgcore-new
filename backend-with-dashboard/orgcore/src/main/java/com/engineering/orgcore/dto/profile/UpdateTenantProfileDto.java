package com.engineering.orgcore.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateTenantProfileDto(
        @NotBlank(message = "Tenant name is required")
        String tenantName,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Phone is required")
        String phone,

        @Email(message = "Email should be valid")
        String email
) {}

