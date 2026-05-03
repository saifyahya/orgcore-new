package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Roles;
import com.engineering.orgcore.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles, Long> {

    Optional<Roles> findByRoleName(RoleEnum name);
}
