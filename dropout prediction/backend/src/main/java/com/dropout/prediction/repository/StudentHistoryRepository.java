package com.dropout.prediction.repository;

import com.dropout.prediction.model.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Long> {
    List<StudentHistory> findByStudentIdOrderByRecordedAtAsc(Long studentId);
}
