package com.engineering.orgcore.dto.branch;

public record BranchDto(
        Long id,
        String branchName,
        String address,
        Integer isActive,
        String createdBy,
        String createdAt,
        String updatedBy,
        String updatedAt
) {}