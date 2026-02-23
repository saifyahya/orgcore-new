package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.branch.BranchDto;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.entity.Branch;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class BranchService {

    private final BranchRepository branchRepository;
    private final Utils utils;

    public BranchDto create(BranchDto request, Long tenantId) {
         if (branchRepository.existsByBranchNameIgnoreCase(request.branchName())) {
             throw new IllegalArgumentException("Branch name already exists");
         }

        Branch branch = new Branch();
        branch.setBranchName(request.branchName().trim());
        branch.setAddress(request.address());
        branch.setTenantId(tenantId);
        branch.setIsActive(1);

        branch.setCreatedBy(utils.getCurrentUserName());
        branch.setCreatedAt(LocalDateTime.now());
        branch.setUpdatedBy(utils.getCurrentUserName());
        branch.setUpdatedAt(LocalDateTime.now());

        Branch saved = branchRepository.save(branch);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BranchDto getById(Long id) throws NotFoundException {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));
        return toResponse(branch);
    }

    @Transactional(readOnly = true)
    public Page<BranchDto> getAll(Long tenantId , PageFilter pageFilter) {
     Page<Branch> page = branchRepository.findAllByTenantId(tenantId,
             pageFilter.getSearch(),
             pageFilter.getIsActive(),
             pageFilter.toPageable());
        return page.map(this::toResponse);
    }

    @Transactional
    public BranchDto update(Long id, BranchDto request) throws NotFoundException {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));

        if (request.branchName() != null && !request.branchName().isBlank()) {
            branch.setBranchName(request.branchName().trim());
        }
        if (request.address() != null) {
            branch.setAddress(request.address());
        }

        if (branch.getIsActive() != null) {
            branch.setIsActive(request.isActive());
        }

        branch.setUpdatedBy(utils.getCurrentUserName());
        branch.setUpdatedAt(LocalDateTime.now());

        branchRepository.save(branch);

        return toResponse(branch);
    }

    public void delete(Long id) throws NotFoundException {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + id));

        if (branch.getInventories() != null && !branch.getInventories().isEmpty()) {
            throw new IllegalStateException("Cannot delete branch because it has inventories.");
        }
        if (branch.getSales() != null && !branch.getSales().isEmpty()) {
            throw new IllegalStateException("Cannot delete branch because it has sales.");
        }

        branch.setIsActive(0);
    }

    public BranchDto toResponse(Branch b) {
        return new BranchDto(b.getId(), b.getBranchName(), b.getAddress(), b.getIsActive(), b.getCreatedBy(), b.getCreatedAt().toString(), b.getUpdatedBy(), b.getUpdatedAt().toString());
    }
}
