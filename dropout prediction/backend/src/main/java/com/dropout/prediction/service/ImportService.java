package com.dropout.prediction.service;

import com.dropout.prediction.model.Student;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class ImportService {

    @Autowired private StudentService studentService;

    public Map<String, Object> importFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return importExcel(file);
        }
        return importCsv(file);
    }

    // ===== Excel Import — reads header row to map columns by name =====
    private Map<String, Object> importExcel(MultipartFile file) throws IOException {
        int success = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return Map.of("imported", 0, "failed", 0, "errors", List.of("Empty file"));

            // Build column index map from header names (case-insensitive)
            Map<String, Integer> colMap = new HashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null) {
                    colMap.put(cell.getStringCellValue().trim().toLowerCase(), c);
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Student s = mapRowByHeader(row, colMap);
                    studentService.addStudent(s);
                    success++;
                } catch (Exception e) {
                    failed++;
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    System.err.println("Import row " + (i + 1) + " error: " + e.getMessage());
                }
            }
        }
        return Map.of("imported", success, "failed", failed, "errors", errors);
    }

    // ===== Map row using header names — tolerates any column order =====
    private Student mapRowByHeader(Row row, Map<String, Integer> col) {
        Student s = new Student();

        s.setName(required(getStr(row, col, "name"), "Name is required"));
        s.setAttendancePercentage(required(getDouble(row, col, "attendance", "attendance %", "attendancepercentage"), "Attendance is required"));
        s.setGpa(required(getDouble(row, col, "gpa", "marks"), "GPA is required"));
        s.setBehaviorScore(required(getInt(row, col, "behavior", "behavior score", "behaviourscore"), "Behavior is required"));
        s.setEngagementLevel(required(getInt(row, col, "engagement", "engagement level", "engagementlevel"), "Engagement is required"));
        s.setFamilyIncome(required(getDouble(row, col, "income", "family income", "familyincome"), "Income is required"));
        s.setStressLevel(required(getInt(row, col, "stress", "stress level", "stresslevel"), "Stress is required"));

        // Optional with defaults
        String fee = getStr(row, col, "feestatus", "fee status", "fee");
        s.setFeeStatus(fee.isBlank() ? "PAID" : fee.toUpperCase());

        s.setScholarship(getBool(row, col, "scholarship"));
        s.setCounseling(getBool(row, col, "counseling", "counselling"));

        String email = getStr(row, col, "parentemail", "parent email", "email");
        if (!email.isBlank()) s.setParentEmail(email);

        String phone = getStr(row, col, "parentphone", "parent phone", "phone");
        if (!phone.isBlank()) s.setParentPhone(phone);

        return s;
    }

    // ===== CSV Import =====
    private Map<String, Object> importCsv(MultipartFile file) throws IOException {
        int success = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return Map.of("imported", 0, "failed", 0, "errors", List.of("Empty file"));

            // Build column map from CSV header
            String[] headers = headerLine.split(",");
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colMap.put(headers[i].trim().toLowerCase(), i);
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                try {
                    String[] p = line.split(",", -1);
                    Student s = mapCsvByHeader(p, colMap);
                    studentService.addStudent(s);
                    success++;
                } catch (Exception e) {
                    failed++;
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                    System.err.println("CSV line " + lineNum + " error: " + e.getMessage());
                }
            }
        }
        return Map.of("imported", success, "failed", failed, "errors", errors);
    }

    private Student mapCsvByHeader(String[] p, Map<String, Integer> col) {
        Student s = new Student();
        s.setName(required(csvStr(p, col, "name"), "Name required"));
        s.setAttendancePercentage(required(csvDouble(p, col, "attendance", "attendance %", "attendancepercentage"), "Attendance required"));
        s.setGpa(required(csvDouble(p, col, "gpa", "marks"), "GPA required"));
        s.setBehaviorScore(required(csvInt(p, col, "behavior", "behavior score", "behaviourscore"), "Behavior required"));
        s.setEngagementLevel(required(csvInt(p, col, "engagement", "engagement level"), "Engagement required"));
        s.setFamilyIncome(required(csvDouble(p, col, "income", "family income", "familyincome"), "Income required"));
        s.setStressLevel(required(csvInt(p, col, "stress", "stress level", "stresslevel"), "Stress required"));

        String fee = csvStr(p, col, "feestatus", "fee status", "fee");
        s.setFeeStatus(fee.isBlank() ? "PAID" : fee.toUpperCase());
        s.setScholarship(csvBool(p, col, "scholarship"));
        s.setCounseling(csvBool(p, col, "counseling", "counselling"));

        String email = csvStr(p, col, "parentemail", "parent email", "email");
        if (!email.isBlank()) s.setParentEmail(email);
        return s;
    }

    // ===== Helpers =====
    private <T> T required(T val, String msg) {
        if (val == null) throw new RuntimeException(msg);
        return val;
    }

    private String getStr(Row row, Map<String, Integer> col, String... keys) {
        for (String key : keys) {
            Integer idx = col.get(key);
            if (idx != null) {
                Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) return cellToString(cell).trim();
            }
        }
        return "";
    }

    private Double getDouble(Row row, Map<String, Integer> col, String... keys) {
        for (String key : keys) {
            Integer idx = col.get(key);
            if (idx != null) {
                Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) {
                    try {
                        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
                        return Double.parseDouble(cell.getStringCellValue().trim());
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    private Integer getInt(Row row, Map<String, Integer> col, String... keys) {
        Double d = getDouble(row, col, keys);
        return d != null ? (int) Math.round(d) : null;
    }

    private boolean getBool(Row row, Map<String, Integer> col, String... keys) {
        for (String key : keys) {
            Integer idx = col.get(key);
            if (idx != null) {
                Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) {
                    if (cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
                    String v = cellToString(cell).trim().toLowerCase();
                    return v.equals("true") || v.equals("yes") || v.equals("1");
                }
            }
        }
        return false;
    }

    private String cellToString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    // CSV helpers
    private String csvStr(String[] p, Map<String, Integer> col, String... keys) {
        for (String key : keys) {
            Integer idx = col.get(key);
            if (idx != null && idx < p.length) return p[idx].trim();
        }
        return "";
    }

    private Double csvDouble(String[] p, Map<String, Integer> col, String... keys) {
        String v = csvStr(p, col, keys);
        if (v.isBlank()) return null;
        try { return Double.parseDouble(v); } catch (Exception e) { return null; }
    }

    private Integer csvInt(String[] p, Map<String, Integer> col, String... keys) {
        Double d = csvDouble(p, col, keys);
        return d != null ? (int) Math.round(d) : null;
    }

    private boolean csvBool(String[] p, Map<String, Integer> col, String... keys) {
        String v = csvStr(p, col, keys).toLowerCase();
        return v.equals("true") || v.equals("yes") || v.equals("1");
    }
}
