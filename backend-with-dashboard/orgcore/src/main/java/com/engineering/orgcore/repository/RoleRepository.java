package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Roles, Long> {
}
