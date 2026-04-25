package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmailAndTenantId(String email, Long tenantId);
    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTenantId(String firstName, String lastName, Long tenantId);
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
    select u
    from Users u
    join fetch u.role
    where u.email = :email
""")    Optional<Users> findByEmail(String email);

}
