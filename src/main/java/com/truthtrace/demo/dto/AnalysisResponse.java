package com.truthtrace.demo.dto;

import java.util.List;

public class AnalysisResponse {

    public String infrastructureRisk;
    public String contentRisk;
    public String overallRisk;
    public int threatScore;
    public List<String> pipeline;
    public String explanation;

    public AnalysisResponse(
            String infrastructureRisk,
            String contentRisk,
            String overallRisk,
            int threatScore,
            List<String> pipeline,
            String explanation) {

        this.infrastructureRisk = infrastructureRisk;
        this.contentRisk = contentRisk;
        this.overallRisk = overallRisk;
        this.threatScore = threatScore;
        this.pipeline = pipeline;
        this.explanation = explanation;
    }
}
