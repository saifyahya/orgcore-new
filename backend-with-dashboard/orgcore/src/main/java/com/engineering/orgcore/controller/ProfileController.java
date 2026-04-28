package com.engineering.orgcore.controller;

import com.engineering.orgcore.dto.profile.ProfileDto;
import com.engineering.orgcore.dto.profile.UpdateProfileDto;
import com.engineering.orgcore.dto.profile.UpdateTenantProfileDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get current user's profile
     * Combines User and Tenant information
     */
    @GetMapping
    public ProfileDto getProfile() throws NotFoundException {
        return profileService.getProfile();
    }

    /**
     * Update current user's profile information
     * Updates: firstName, lastName, email
     */
    @PutMapping
    public ProfileDto updateProfile(@Valid @RequestBody UpdateProfileDto request) throws NotFoundException {
        return profileService.updateUserProfile(request);
    }

    /**
     * Update tenant's profile information
     * Updates: tenantName, address, phone, email
     */
    @PutMapping("/tenant")
    public ProfileDto updateTenantProfile(@Valid @RequestBody UpdateTenantProfileDto request) throws NotFoundException {
        return profileService.updateTenantProfile(request);
    }
}

