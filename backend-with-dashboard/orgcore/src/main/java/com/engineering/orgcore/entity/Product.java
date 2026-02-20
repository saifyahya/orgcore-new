package com.engineering.orgcore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    private String image;

    private Double price;

    private Integer isActive;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<SaleItem> saleItems;

}
