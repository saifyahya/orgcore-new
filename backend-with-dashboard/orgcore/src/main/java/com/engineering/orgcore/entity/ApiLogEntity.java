package com.engineering.orgcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

/**
 * @author Rayyan
 * @version 1.0
 */
@Entity
@Table(name = "api_log")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ApiLogEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  //  private Long llId;
    private String apiType;

    private String apiName;

    private String queryParameters;

    @Column(columnDefinition = "NVARCHAR(max)")
    private String requestData;

    @Column(columnDefinition = "NVARCHAR(max)")
    private String responseData;

    private Integer elapsedType;

    private Long elapsedTime;

    private Long resultCode;

    private String resultDesc;
}//ApiLogEntity
