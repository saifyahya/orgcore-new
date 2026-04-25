package com.engineering.orgcore.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String firstName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    private Roles role;

    private String email;
    private String password;
    private Integer isActive;
}
