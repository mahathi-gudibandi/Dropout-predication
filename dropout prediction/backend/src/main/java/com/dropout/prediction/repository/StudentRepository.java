package com.dropout.prediction.repository;

import com.dropout.prediction.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByDropoutRisk(String dropoutRisk);
}
