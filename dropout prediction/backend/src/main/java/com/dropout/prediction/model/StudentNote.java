package com.dropout.prediction.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student_notes")
public class StudentNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 1000)
    private String note;

    @Column(nullable = false)
    private String addedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
