package com.dropout.prediction.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Stores a snapshot of student data each time it is updated,
 * enabling risk trend tracking over time.
 */
@Data
@Entity
@Table(name = "student_history")
public class StudentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    private Double attendancePercentage;
    private Double gpa;
    private Integer behaviorScore;
    private Integer engagementLevel;
    private Integer stressLevel;
    private String dropoutRisk;

    @Column(nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();
}
