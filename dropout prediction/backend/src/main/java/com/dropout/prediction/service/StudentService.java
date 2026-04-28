package com.dropout.prediction.service;

import com.dropout.prediction.model.Student;
import com.dropout.prediction.model.StudentHistory;
import com.dropout.prediction.repository.StudentHistoryRepository;
import com.dropout.prediction.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private PredictionService predictionService;
    @Autowired private StudentHistoryRepository historyRepository;
    @Autowired private AlertService alertService;

    public Student addStudent(Student student) {
        var prediction = predictionService.predict(student);
        student.setDropoutRisk(prediction.getDropoutRisk());
        Student saved = studentRepository.save(student);
        saveHistory(saved);
        alertService.sendAttendanceAlert(saved); // fire alert if attendance < 75
        return saved;
    }

    public Student updateStudent(Long id, Student updated) {
        Student existing = getStudentById(id);
        existing.setName(updated.getName());
        existing.setAttendancePercentage(updated.getAttendancePercentage());
        existing.setGpa(updated.getGpa());
        existing.setBehaviorScore(updated.getBehaviorScore());
        existing.setEngagementLevel(updated.getEngagementLevel());
        existing.setFamilyIncome(updated.getFamilyIncome());
        existing.setScholarship(updated.getScholarship());
        existing.setFeeStatus(updated.getFeeStatus());
        existing.setCounseling(updated.getCounseling());
        existing.setStressLevel(updated.getStressLevel());
        if (updated.getParentEmail() != null) existing.setParentEmail(updated.getParentEmail());
        if (updated.getParentPhone() != null) existing.setParentPhone(updated.getParentPhone());

        var prediction = predictionService.predict(existing);
        existing.setDropoutRisk(prediction.getDropoutRisk());
        Student saved = studentRepository.save(existing);
        saveHistory(saved);
        alertService.sendAttendanceAlert(saved);
        return saved;
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public List<StudentHistory> getHistory(Long studentId) {
        return historyRepository.findByStudentIdOrderByRecordedAtAsc(studentId);
    }

    private void saveHistory(Student s) {
        StudentHistory h = new StudentHistory();
        h.setStudentId(s.getId());
        h.setAttendancePercentage(s.getAttendancePercentage());
        h.setGpa(s.getGpa());
        h.setBehaviorScore(s.getBehaviorScore());
        h.setEngagementLevel(s.getEngagementLevel());
        h.setStressLevel(s.getStressLevel());
        h.setDropoutRisk(s.getDropoutRisk());
        historyRepository.save(h);
    }
}
