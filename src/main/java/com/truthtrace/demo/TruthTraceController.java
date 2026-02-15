package com.truthtrace.demo;

import com.truthtrace.demo.dto.AnalysisResponse;
import com.truthtrace.demo.engine.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
public class TruthTraceController {

    @PostMapping("/analyze")
    public AnalysisResponse analyze(@RequestBody Map<String, String> request) {

        String url = request.get("url");

        List<String> pipeline = new ArrayList<>();

        // ===============================================
        // 1️⃣ Infrastructure Analysis
        // ===============================================

        long startInfra = System.currentTimeMillis();
        Risk infraRisk = URLAnalyzer.analyze(url);
        long infraTime = System.currentTimeMillis() - startInfra;

        pipeline.add("Infrastructure analysis completed in " + infraTime + " ms.");
        pipeline.add("Infrastructure risk level: " + infraRisk);

        // ===============================================
        // 2️⃣ Domain Analysis
        // ===============================================

        List<String> domainIssues = DomainAnalyzer.analyze(url);
        pipeline.addAll(domainIssues);

        // ===============================================
        // 3️⃣ Content Extraction + Analysis
        // ===============================================

        long startContent = System.currentTimeMillis();
        String content = ArticleExtractionService.extractArticleText(url);
        pipeline.add("Content extraction executed.");

        Risk contentRisk = ContentAnalyzer.analyze(content, pipeline);
        long contentTime = System.currentTimeMillis() - startContent;

        pipeline.add("Content analysis completed in " + contentTime + " ms.");
        pipeline.add("Content risk level: " + contentRisk);

        // ===============================================
        // 4️⃣ Combine Risks
        // ===============================================

        Risk overallRisk = combineRisks(infraRisk, contentRisk);
        pipeline.add("Overall risk classification: " + overallRisk);

        // ===============================================
        // 5️⃣ Numeric Threat Score
        // ===============================================

        int threatScore = calculateThreatScore(infraRisk, contentRisk);
        pipeline.add("Composite threat score: " + threatScore + "/100");

        // ===============================================
        // 6️⃣ AI Explanation
        // ===============================================

        String explanation = OpenAIExplainer.generateExplanation(
                url,
                overallRisk.toString(),
                String.join("\n", pipeline)
        );

        return new AnalysisResponse(
                infraRisk.toString(),
                contentRisk.toString(),
                overallRisk.toString(),
                threatScore,
                pipeline,
                explanation
        );
    }

    // ===============================================
    // Risk Combination Logic
    // ===============================================

    private Risk combineRisks(Risk infra, Risk content) {

        if (infra == Risk.HIGH || content == Risk.HIGH)
            return Risk.HIGH;

        if (infra == Risk.MEDIUM || content == Risk.MEDIUM)
            return Risk.MEDIUM;

        return Risk.LOW;
    }

    // ===============================================
    // Threat Score Calculator
    // ===============================================

    private int calculateThreatScore(Risk infra, Risk content) {

        int score = 0;

        if (infra == Risk.HIGH) score += 50;
        else if (infra == Risk.MEDIUM) score += 30;
        else score += 10;

        if (content == Risk.HIGH) score += 40;
        else if (content == Risk.MEDIUM) score += 20;
        else score += 5;

        return score;
    }
}
