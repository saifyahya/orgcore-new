package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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


    @Query("""
    select u
    from Users u
    left join fetch u.branch b
    where u.tenantId = :tenantId and
    :branchId is null or (b.id = :branchId)
""")
    Page<Users> findByTenantIdAndBranchIdIn(Long tenantId, Long branchId, Pageable pageable);

}
