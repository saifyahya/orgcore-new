package com.engineering.orgcore.service;

import com.engineering.orgcore.dto.auth.AuthRequest;
import com.engineering.orgcore.dto.auth.AuthResponse;
import com.engineering.orgcore.dto.auth.LoginRequest;
import com.engineering.orgcore.dto.auth.TenantDto;
import com.engineering.orgcore.entity.Tenant;
import com.engineering.orgcore.entity.Users;
import com.engineering.orgcore.exceptions.ApiException;
import com.engineering.orgcore.exceptions.MissingDataException;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.TenantRepository;
import com.engineering.orgcore.repository.UsersRepository;
import com.engineering.orgcore.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthenticationService {

        private final TenantRepository tenantRepository;
        private final UsersRepository usersRepository;
        private final JwtUtil jwtUtil;

        @Transactional(rollbackFor = Exception.class)
    public AuthResponse signup( AuthRequest request) throws Exception {

        if(request == null || request.tenant() == null){
            throw new MissingDataException("tenant is missing");
        }
        TenantDto tenantDto = request.tenant();
        if(tenantRepository.existsByEmailAndIsActive(tenantDto.email(),1)){
            throw new ApiException("email already exists");
        }
        if(tenantRepository.existsByTenantNameAndIsActive(tenantDto.tenantName(),1)){
            throw new ApiException("tenant name already exists");
        }
        if(tenantRepository.existsByPhoneAndIsActive(tenantDto.phone(), 1)){
            throw new ApiException("phone already exists");
        }


        // create tenant
        TenantDto t = request.tenant();
        Tenant tenant = new Tenant();
        tenant.setTenantName(t.tenantName());
        tenant.setAddress(t.address());
        tenant.setEmail(t.email());
        tenant.setPhone(t.phone());
        tenant.setIsActive(1);
        tenant = tenantRepository.save(tenant);

        // create user assigned to tenant
        Users u = new Users();
        u.setFirstName(request.firstName());
        u.setLastName(request.lastName());
        u.setEmail(request.email());
        u.setPassword(request.password()); // plain for now
        u.setIsActive(1);
        u.setTenantId(tenant.getId());

        u = usersRepository.save(u);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", u.getEmail());
        claims.put("username", u.getFirstName() + "  " + u.getLastName());
        claims.put("tenantId", tenant.getId());

        String token = jwtUtil.generateToken(claims, u.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login( LoginRequest body) throws NotFoundException, ApiException {
        String email = body.email();
        String password = body.password();

        Tenant tenant = tenantRepository.findByTenantName(body.customerName()).orElseThrow(() -> new NotFoundException("tenant not found"));

        Optional<Users> userOpt = usersRepository.findByEmailAndTenantId(email, tenant.getId());
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User Not Found");
        }
        Users u = userOpt.get();
        if (!u.getPassword().equals(password)) {
            throw new ApiException("Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", u.getEmail());
        claims.put("username", u.getFirstName() + "  " + u.getLastName());
        claims.put("tenantId", u.getTenantId());

         String token = jwtUtil.generateToken(claims, u.getEmail());

        return new AuthResponse(token);
    }
}
