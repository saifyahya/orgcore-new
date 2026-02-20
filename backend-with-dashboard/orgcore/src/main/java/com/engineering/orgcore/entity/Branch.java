package com.engineering.orgcore.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Branch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchName;

    private String address;

    @OneToMany(mappedBy = "branch")
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "branch")
    private List<Sale> sales;

    private Integer isActive;

}
