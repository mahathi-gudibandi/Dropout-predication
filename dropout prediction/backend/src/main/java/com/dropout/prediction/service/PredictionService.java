package com.dropout.prediction.service;

import com.dropout.prediction.dto.PredictionResponse;
import com.dropout.prediction.model.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Prediction service using a rule-based scoring model.
 * Each risk factor contributes a weighted score (0-100).
 * Score < 35 = LOW, 35-65 = MEDIUM, > 65 = HIGH
 */
@Service
public class PredictionService {

    public PredictionResponse predict(Student student) {
        double score = calculateRiskScore(student);
        String risk = classifyRisk(score);
        List<String> interventions = generateInterventions(student, risk);
        return new PredictionResponse(risk, score / 100.0, interventions);
    }

    private double calculateRiskScore(Student student) {
        double score = 0;

        // Attendance (weight: 25)
        if (student.getAttendancePercentage() < 60) score += 25;
        else if (student.getAttendancePercentage() < 75) score += 15;
        else if (student.getAttendancePercentage() < 85) score += 5;

        // GPA/Marks (weight: 20) — GPA out of 10
        if (student.getGpa() < 4.0) score += 20;
        else if (student.getGpa() < 6.0) score += 12;
        else if (student.getGpa() < 7.5) score += 5;

        // Behavior score (weight: 10) — lower is worse
        if (student.getBehaviorScore() <= 3) score += 10;
        else if (student.getBehaviorScore() <= 5) score += 5;

        // Engagement level (weight: 10) — lower is worse
        if (student.getEngagementLevel() <= 3) score += 10;
        else if (student.getEngagementLevel() <= 5) score += 5;

        // Financial: fee status (weight: 10)
        if ("PENDING".equalsIgnoreCase(student.getFeeStatus())) score += 10;

        // Scholarship (weight: 5) — no scholarship adds risk
        if (!student.getScholarship()) score += 5;

        // Family income (weight: 10) — below 20000 is low income
        if (student.getFamilyIncome() < 10000) score += 10;
        else if (student.getFamilyIncome() < 20000) score += 5;

        // Stress level (weight: 10) — higher is worse
        if (student.getStressLevel() >= 8) score += 10;
        else if (student.getStressLevel() >= 6) score += 5;

        // No counseling when stressed (weight: 5)
        if (!student.getCounseling() && student.getStressLevel() >= 6) score += 5;

        return Math.min(score, 100);
    }

    private String classifyRisk(double score) {
        if (score < 35) return "LOW";
        if (score < 65) return "MEDIUM";
        return "HIGH";
    }

    private List<String> generateInterventions(Student student, String risk) {
        List<String> suggestions = new ArrayList<>();

        if (student.getAttendancePercentage() < 75)
            suggestions.add("Academic Support: Attendance is critically low. Assign an attendance monitor and notify parents.");

        if (student.getGpa() < 6.0)
            suggestions.add("Academic Support: GPA is below average. Enroll student in tutoring or remedial classes.");

        if (student.getBehaviorScore() <= 5)
            suggestions.add("Mentoring: Low behavior score. Assign a faculty mentor for regular check-ins.");

        if (student.getEngagementLevel() <= 5)
            suggestions.add("Mentoring: Low engagement. Encourage participation in clubs, projects, or peer groups.");

        if (student.getFamilyIncome() < 20000 || "PENDING".equalsIgnoreCase(student.getFeeStatus()))
            suggestions.add("Financial Aid: Student may be facing financial hardship. Review eligibility for scholarships or fee waivers.");

        if (student.getStressLevel() >= 6 || !student.getCounseling())
            suggestions.add("Counseling: High stress detected. Refer student to a counselor or therapist immediately.");

        if (suggestions.isEmpty())
            suggestions.add("No immediate intervention required. Continue monitoring student progress.");

        return suggestions;
    }
}
