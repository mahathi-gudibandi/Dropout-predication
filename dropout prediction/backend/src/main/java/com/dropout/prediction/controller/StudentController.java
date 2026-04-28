package com.dropout.prediction.controller;

import com.dropout.prediction.dto.PredictionResponse;
import com.dropout.prediction.model.Student;
import com.dropout.prediction.model.StudentHistory;
import com.dropout.prediction.model.StudentNote;
import com.dropout.prediction.service.ImportService;
import com.dropout.prediction.service.NoteService;
import com.dropout.prediction.service.PredictionService;
import com.dropout.prediction.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class StudentController {

    @Autowired private StudentService studentService;
    @Autowired private PredictionService predictionService;
    @Autowired private NoteService noteService;
    @Autowired private ImportService importService;

    /** POST /api/addStudent */
    @PostMapping("/addStudent")
    public ResponseEntity<Student> addStudent(@Valid @RequestBody Student student) {
        return ResponseEntity.ok(studentService.addStudent(student));
    }

    /** GET /api/students */
    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    /** GET /api/students/{id} */
    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    /** PUT /api/students/{id} — Edit student */
    @PutMapping("/students/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id,
                                                  @Valid @RequestBody Student student) {
        return ResponseEntity.ok(studentService.updateStudent(id, student));
    }

    /** DELETE /api/students/{id} */
    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/students/{id}/history — Risk trend */
    @GetMapping("/students/{id}/history")
    public ResponseEntity<List<StudentHistory>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getHistory(id));
    }

    /** POST /api/students/{id}/notes — Add note */
    @PostMapping("/students/{id}/notes")
    public ResponseEntity<StudentNote> addNote(@PathVariable Long id,
                                                @RequestBody Map<String, String> body,
                                                Authentication auth) {
        String note = body.get("note");
        return ResponseEntity.ok(noteService.addNote(id, note, auth.getName()));
    }

    /** GET /api/students/{id}/notes */
    @GetMapping("/students/{id}/notes")
    public ResponseEntity<List<StudentNote>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNotes(id));
    }

    /** DELETE /api/notes/{noteId} */
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        noteService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/predict */
    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@Valid @RequestBody Student student) {
        return ResponseEntity.ok(predictionService.predict(student));
    }

    /** GET /api/students/export/csv — Export all students as CSV */
    @GetMapping("/students/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=students.csv");

        List<Student> students = studentService.getAllStudents();
        PrintWriter writer = response.getWriter();

        writer.println("ID,Name,Attendance,GPA,Behavior,Engagement,FamilyIncome,Scholarship,FeeStatus,Counseling,StressLevel,DropoutRisk");
        for (Student s : students) {
            writer.printf("%d,%s,%.1f,%.1f,%d,%d,%.0f,%s,%s,%s,%d,%s%n",
                    s.getId(), s.getName(), s.getAttendancePercentage(), s.getGpa(),
                    s.getBehaviorScore(), s.getEngagementLevel(), s.getFamilyIncome(),
                    s.getScholarship(), s.getFeeStatus(), s.getCounseling(),
                    s.getStressLevel(), s.getDropoutRisk());
        }
        writer.flush();
    }

    /** POST /api/students/import — Bulk CSV or Excel import */
    @PostMapping("/students/import")
    public ResponseEntity<Map<String, Object>> importFile(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(importService.importFile(file));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to read file: " + e.getMessage()));
        }
    }
}
