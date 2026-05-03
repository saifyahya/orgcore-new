package com.engineering.orgcore.dto.filter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class PageFilter {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(200)
    private int size = 20;

    // e.g. "id" or "branchName" or "createdAt"
    private String sortBy = "id";

    // "asc" or "desc"
    private String sortDir = "desc";

    // Optional general search term
    private String search;

    private Integer isActive;

    private Long categoryId; // just for product filtering

    private Long branchId; // optional branch filtering

    public  Pageable toPageable() {
        String sortBy = (this.getSortBy() == null || this.getSortBy().isBlank()) ? "id" : this.getSortBy();
        Sort.Direction dir = "desc".equalsIgnoreCase(this.getSortDir())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(
                Math.max(0, this.getPage()),
                Math.min(Math.max(1, this.getSize()), 200),
                Sort.by(dir, sortBy)
        );
    }

}