package com.dropout.prediction.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double attendancePercentage;

    @Min(0) @Max(10)
    @Column(nullable = false)
    private Double gpa;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer behaviorScore;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer engagementLevel;

    @Column(nullable = false)
    private Double familyIncome;

    @Column(nullable = false)
    private Boolean scholarship;

    // "PAID" or "PENDING"
    @Column(nullable = false)
    private String feeStatus;

    @Column(nullable = false)
    private Boolean counseling;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer stressLevel;

    // Parent contact
    @Column
    private String parentEmail;

    @Column
    private String parentPhone;

    // "LOW", "MEDIUM", "HIGH"
    @Column
    private String dropoutRisk;
}
