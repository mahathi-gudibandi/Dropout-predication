package com.dropout.prediction.service;

import com.dropout.prediction.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class AlertService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@dropoutguard.com}")
    private String fromEmail;

    /**
     * Sends a low attendance alert to the parent if:
     * - attendance < 75%
     * - parent email is provided
     * - mail is enabled
     */
    public void sendAttendanceAlert(Student student) {
        if (!mailEnabled || mailSender == null) return;
        if (student.getParentEmail() == null || student.getParentEmail().isBlank()) return;
        if (student.getAttendancePercentage() >= 75) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(student.getParentEmail());
            helper.setSubject("⚠️ Low Attendance Alert — " + student.getName());
            helper.setText(buildEmailBody(student), true); // true = HTML

            mailSender.send(message);
            System.out.println("Attendance alert sent to: " + student.getParentEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email alert: " + e.getMessage());
        }
    }

    private String buildEmailBody(Student student) {
        String riskColor = switch (student.getDropoutRisk()) {
            case "HIGH"   -> "#ff6584";
            case "MEDIUM" -> "#f9ca24";
            default       -> "#43e97b";
        };

        return """
            <div style="font-family:Segoe UI,sans-serif;max-width:600px;margin:auto;background:#1a1a2e;color:#f0f0ff;border-radius:16px;overflow:hidden">
              <div style="background:linear-gradient(135deg,#6c63ff,#ff6584);padding:2rem;text-align:center">
                <h1 style="margin:0;font-size:1.5rem">🎓 DropoutGuard</h1>
                <p style="margin:0.5rem 0 0;opacity:0.85">Student Attendance Alert</p>
              </div>
              <div style="padding:2rem">
                <p style="font-size:1rem;margin-bottom:1rem">Dear Parent/Guardian,</p>
                <p>We are writing to inform you that your child <strong>%s</strong> has a current attendance of
                   <strong style="color:#ff6584">%.1f%%</strong>, which is below the required threshold of <strong>75%%</strong>.</p>

                <div style="background:rgba(255,255,255,0.07);border-radius:12px;padding:1.25rem;margin:1.5rem 0">
                  <table style="width:100%;border-collapse:collapse">
                    <tr><td style="padding:0.4rem 0;color:rgba(255,255,255,0.6)">Student Name</td><td style="font-weight:700">%s</td></tr>
                    <tr><td style="padding:0.4rem 0;color:rgba(255,255,255,0.6)">Attendance</td><td style="font-weight:700;color:#ff6584">%.1f%%</td></tr>
                    <tr><td style="padding:0.4rem 0;color:rgba(255,255,255,0.6)">GPA</td><td style="font-weight:700">%.1f / 10</td></tr>
                    <tr><td style="padding:0.4rem 0;color:rgba(255,255,255,0.6)">Dropout Risk</td>
                        <td><span style="background:%s;color:#000;padding:0.2rem 0.75rem;border-radius:999px;font-weight:700;font-size:0.85rem">%s</span></td></tr>
                  </table>
                </div>

                <p>Low attendance significantly increases the risk of academic failure and dropout. We strongly encourage you to:</p>
                <ul style="padding-left:1.25rem;line-height:2">
                  <li>Discuss the importance of regular attendance with your child</li>
                  <li>Contact the school counselor for support</li>
                  <li>Reach out to the class teacher for missed work</li>
                </ul>

                <p style="margin-top:1.5rem;color:rgba(255,255,255,0.6);font-size:0.85rem">
                  This is an automated alert from the DropoutGuard Early Intervention System.<br/>
                  Please do not reply to this email.
                </p>
              </div>
            </div>
            """.formatted(
                student.getName(),
                student.getAttendancePercentage(),
                student.getName(),
                student.getAttendancePercentage(),
                student.getGpa(),
                riskColor,
                student.getDropoutRisk()
        );
    }
}
