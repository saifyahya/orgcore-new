package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.profile.ProfileDto;
import com.engineering.orgcore.dto.profile.UpdateProfileDto;
import com.engineering.orgcore.dto.profile.UpdateTenantProfileDto;
import com.engineering.orgcore.entity.Tenant;
import com.engineering.orgcore.entity.Users;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.TenantRepository;
import com.engineering.orgcore.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UsersRepository usersRepository;
    private final TenantRepository tenantRepository;
    private final Utils utils;

    @Transactional(readOnly = true)
    public ProfileDto getProfile() throws NotFoundException {
        Long currentTenantId = utils.getCurrentTenant();
        String currentUserEmail = utils.getCurrentUserEmail();

        Users user = usersRepository.findByEmailAndTenantId(currentUserEmail, currentTenantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        return toProfileDto(user, tenant);
    }

    @Transactional
    public ProfileDto updateUserProfile(UpdateProfileDto request) throws NotFoundException {
        Long currentTenantId = utils.getCurrentTenant();
        String currentUserEmail = utils.getCurrentUserEmail();

        Users user = usersRepository.findByEmailAndTenantId(currentUserEmail, currentTenantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if email is unique (if changed)
        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            if (usersRepository.existsByEmailIgnoreCase(request.email())) {
                throw new IllegalArgumentException("Email already exists");
            }
        }

        // Check if first name and last name combination is unique (if changed)
        if (!user.getFirstName().equalsIgnoreCase(request.firstName()) ||
                !user.getLastName().equalsIgnoreCase(request.lastName())) {
            if (usersRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTenantId(
                    request.firstName(), request.lastName(), currentTenantId)) {
                throw new IllegalArgumentException("A user with this first name and last name already exists in your tenant");
            }
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        usersRepository.save(user);

        Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        return toProfileDto(user, tenant);
    }

    @Transactional
    public ProfileDto updateTenantProfile(UpdateTenantProfileDto request) throws NotFoundException {
        Long currentTenantId = utils.getCurrentTenant();
        String currentUserEmail = utils.getCurrentUserEmail();

        Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Check if tenant name is unique
        if (!tenant.getTenantName().equalsIgnoreCase(request.tenantName())) {
            if (tenantRepository.existsByTenantNameAndIsActive(request.tenantName(), 1)) {
                throw new IllegalArgumentException("Tenant name already exists");
            }
        }

        // Check if email is unique
        if (!tenant.getEmail().equalsIgnoreCase(request.email())) {
            if (tenantRepository.existsByEmailIgnoreCaseAndIsActive(request.email(), 1)) {
                throw new IllegalArgumentException("Email already exists");
            }
        }

        // Check if phone is unique
        if (!tenant.getPhone().equals(request.phone())) {
            if (tenantRepository.existsByPhoneAndIsActive(request.phone(), 1)) {
                throw new IllegalArgumentException("Phone already exists");
            }
        }

        tenant.setTenantName(request.tenantName());
        tenant.setAddress(request.address());
        tenant.setPhone(request.phone());
        tenant.setEmail(request.email());
        tenant.setUpdatedBy(currentUserEmail);
        tenant.setUpdatedAt(LocalDateTime.now());

        tenantRepository.save(tenant);

        Users user = usersRepository.findByEmailAndTenantId(currentUserEmail, currentTenantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return toProfileDto(user, tenant);
    }

    private ProfileDto toProfileDto(Users user, Tenant tenant) {
        return new ProfileDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getIsActive(),
                tenant.getId(),
                tenant.getTenantName(),
                tenant.getAddress(),
                tenant.getPhone(),
                tenant.getIsActive(),
                user.getCreatedBy(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.getUpdatedBy(),
                user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
        );
    }
}

