package com.dropout.prediction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PredictionResponse {
    private String dropoutRisk;       // LOW, MEDIUM, HIGH
    private double riskScore;         // 0.0 - 1.0
    private List<String> interventions;
}
