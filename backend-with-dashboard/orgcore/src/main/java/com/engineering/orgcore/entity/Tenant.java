package com.engineering.orgcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table
@Entity
public class Tenant  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String tenantName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    private String email;

    private String phone;

    private Integer isActive;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    @Column(insertable = false)
    private String updatedBy;

}
