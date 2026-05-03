package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.branch.BranchDto;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;
    private final Utils utils;

    // Create
    @PostMapping
    public BranchDto create(@Valid @RequestBody BranchDto request) {
        return branchService.create(request, utils.getCurrentTenant());
    }

    // Get by id
    @GetMapping("/{id}")
    public BranchDto getById(@PathVariable Long id) throws NotFoundException {
        return branchService.getById(id);
    }

    // Get all (paged) by tenantId
    // Example:
    // GET /api/branches?tenantId=1&page=0&size=20&sortBy=id&sortDir=asc&q=amman
    @GetMapping
    public Page<BranchDto> getAll(
            @ModelAttribute PageFilter pageFilter
    ) {
        return branchService.getAll(utils.getCurrentTenant(), pageFilter);
    }

    // Update
    @PutMapping("/{id}")
    public BranchDto update(@PathVariable Long id, @Valid @RequestBody BranchDto request) throws NotFoundException {
        return branchService.update(id, request);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws NotFoundException {
        branchService.delete(id);
    }
}
