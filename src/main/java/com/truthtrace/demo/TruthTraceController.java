package com.truthtrace.demo;

import com.truthtrace.demo.engine.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class TruthTraceController {

    @PostMapping("/analyze")
    public Map<String, Object> analyze(@RequestBody Map<String, String> request) {

        String url = request.get("url");

        Risk urlRisk = URLAnalyzer.analyze(url);

        String extractedText =
                ArticleExtractionService.extractArticleText(url);

        Risk contentRisk =
                ContentAnalyzer.analyze(extractedText);

        Risk finalRisk =
                combineRisk(urlRisk, contentRisk);

        String explanation =
                OpenAIExplainer.generateExplanation(
                        url,
                        finalRisk.name(),
                        "Infrastructure Risk: " + urlRisk +
                                "\nContent Risk: " + contentRisk
                );

        Map<String, Object> response = new HashMap<>();
        response.put("infrastructureRisk", urlRisk);
        response.put("contentRisk", contentRisk);
        response.put("overallRisk", finalRisk);
        response.put("explanation", explanation);

        return response;
    }

    private Risk combineRisk(Risk urlRisk, Risk contentRisk) {

        if (urlRisk == Risk.HIGH || contentRisk == Risk.HIGH)
            return Risk.HIGH;

        if (urlRisk == Risk.MEDIUM || contentRisk == Risk.MEDIUM)
            return Risk.MEDIUM;

        return Risk.LOW;
    }
}
