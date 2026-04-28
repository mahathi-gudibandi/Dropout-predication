package com.dropout.prediction.repository;

import com.dropout.prediction.model.StudentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentNoteRepository extends JpaRepository<StudentNote, Long> {
    List<StudentNote> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
