package com.engineering.orgcore.dto.user;

import com.engineering.orgcore.dto.branch.BranchDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer isActive;
    private BranchDto branch;
    private String createdBy;
    private String createdAt;
    private String updatedBy;
    private String updatedAt;
}
