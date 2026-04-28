package com.dropout.prediction.service;

import com.dropout.prediction.model.StudentNote;
import com.dropout.prediction.repository.StudentNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    @Autowired private StudentNoteRepository noteRepository;

    public StudentNote addNote(Long studentId, String noteText, String username) {
        StudentNote note = new StudentNote();
        note.setStudentId(studentId);
        note.setNote(noteText);
        note.setAddedBy(username);
        return noteRepository.save(note);
    }

    public List<StudentNote> getNotes(Long studentId) {
        return noteRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }
}
