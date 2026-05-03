package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.branch.BranchDto;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.user.AddUserRequest;
import com.engineering.orgcore.dto.user.UserDto;
import com.engineering.orgcore.entity.Branch;
import com.engineering.orgcore.entity.Tenant;
import com.engineering.orgcore.entity.Users;
import com.engineering.orgcore.enums.RoleEnum;
import com.engineering.orgcore.exceptions.ApiException;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.BranchRepository;
import com.engineering.orgcore.repository.RoleRepository;
import com.engineering.orgcore.repository.TenantRepository;
import com.engineering.orgcore.repository.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final Utils utils;
    private final BCryptPasswordEncoder passwordEncoder;


    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(Long tenantId, AddUserRequest newUser) throws NotFoundException, ApiException {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new NotFoundException("tenant not found"));
        if (tenant.getIsActive() != 1) {
            throw new NotFoundException("tenant not found");
        }
        if (usersRepository.existsByEmailIgnoreCase(newUser.email())) {
            throw new ApiException("email already exists for this tenant");
        }
        if (usersRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTenantId(newUser.firstName(), newUser.lastName(), tenantId)) {
            throw new ApiException("Name already exists for this tenant");
        }

        Branch branch = branchRepository.findById(newUser.branchId()).orElseThrow(() -> new NotFoundException("branch not found"));
        if (!branch.getCreatedBy().equals(utils.getCurrentUserEmail())) {
            throw new NotFoundException("branch not found");
        }
        Users u = new Users();
        u.setFirstName(newUser.firstName());
        u.setLastName(newUser.lastName());
        u.setEmail(newUser.email());
        u.setPassword(passwordEncoder.encode(newUser.password()));
        u.setIsActive(1);
        u.setTenantId(tenantId);
        u.setBranch(branch);
        u.setRole(roleRepository.findByRoleName(RoleEnum.CASHER).orElseThrow(() -> new NotFoundException("role Casher not found")));
        u.setCreatedBy(utils.getCurrentUserEmail());
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedBy(utils.getCurrentUserEmail());
        u.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(u);
        return true;
    }


    public UserDto update(UserDto request) throws NotFoundException {
        Users user = usersRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getId()));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getIsActive() != null && !user.getRole().getRoleName().equals(RoleEnum.ADMIN)) {
            user.setIsActive(request.getIsActive());
        }

        user.setUpdatedBy(utils.getCurrentUserEmail());
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        return toUserResponse(user);
    }


    public Page<UserDto> getAllUsers(Long tenantId, PageFilter page) throws NotFoundException {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new NotFoundException("tenant not found"));
        if (tenant.getIsActive() != 1) {
            throw new NotFoundException("tenant not found");
        }

        Page<Users> usersPage;
        Pageable pageable = page.toPageable();
        usersPage = usersRepository.findByTenantIdAndBranchIdIn(tenantId, page.getBranchId(), pageable);
        return usersPage.map(this::toUserResponse);
    }

    private UserDto toUserResponse(Users user) {
        BranchDto branchDto = null;
        if (user.getBranch() != null) {
            Branch b = user.getBranch();
            branchDto = new BranchDto(
                    b.getId(),
                    b.getBranchName(),
                    b.getAddress(),
                    b.getIsActive(),
                    b.getCreatedBy(),
                    b.getCreatedAt() != null ? b.getCreatedAt().toString() : null,
                    b.getUpdatedBy(),
                    b.getUpdatedAt() != null ? b.getUpdatedAt().toString() : null
            );
        }
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getIsActive(),
                branchDto,
                user.getCreatedBy(),
                user.getCreatedAt().toString(),
                user.getUpdatedBy(),
                user.getUpdatedAt().toString()
        );
    }
}
