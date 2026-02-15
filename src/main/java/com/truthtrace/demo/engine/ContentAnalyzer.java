package com.truthtrace.demo.engine;

import java.util.ArrayList;
import java.util.List;

public class ContentAnalyzer {

    public static Risk analyze(String articleText) {

        if (articleText == null || articleText.isBlank()) {
            System.out.println("No content available.");
            return Risk.MEDIUM;
        }

        System.out.println("Extracted Content Length: "
                + articleText.length());

        int impact = 0;
        List<String> issues = new ArrayList<>();

        String lower = articleText.toLowerCase();

        // ===================================================
        // 1️⃣ BASIC CONTENT DEPTH CHECK
        // ===================================================

        if (lower.length() < 300) {
            issues.add("Very short article content.");
            impact += 5;
        }

        // ===================================================
        // 2️⃣ SOCIAL ENGINEERING / MANIPULATION SIGNALS
        // ===================================================

        impact += detect(lower,
                new String[]{
                        "act now",
                        "urgent action required",
                        "limited time",
                        "final notice",
                        "immediate response"
                },
                issues, 4, "Urgency signal");

        impact += detect(lower,
                new String[]{
                        "verify your account",
                        "confirm your identity",
                        "enter your password"
                },
                issues, 10, "Credential harvesting signal");

        impact += detect(lower,
                new String[]{
                        "guaranteed return",
                        "double your investment",
                        "crypto giveaway",
                        "risk-free profit"
                },
                issues, 8, "Scam hook detected");

        impact += detect(lower,
                new String[]{
                        "shocking",
                        "you won't believe",
                        "what they don't want you to know",
                        "exposed"
                },
                issues, 5, "Emotional manipulation signal");

        // Excessive exclamation marks
        int exclamations = articleText.length()
                - articleText.replace("!", "").length();
        if (exclamations > 5) {
            issues.add("Excessive exclamation marks detected.");
            impact += 6;
        }

        // ===================================================
        // 3️⃣ AI-GENERATED CONTENT HEURISTICS
        // ===================================================

        impact += detect(lower,
                new String[]{
                        "in today's world",
                        "it is important to note",
                        "in conclusion",
                        "furthermore",
                        "moreover"
                },
                issues, 3, "AI-generic phrasing");

        double avgSentenceLength =
                calculateAverageSentenceLength(articleText);

        if (avgSentenceLength > 18 && avgSentenceLength < 23) {
            issues.add("Highly uniform sentence structure detected (possible AI pattern).");
            impact += 5;
        }

        int paragraphCount =
                articleText.split("\\n\\n").length;

        if (paragraphCount >= 5) {
            issues.add("Structured paragraph symmetry detected.");
            impact += 4;
        }

        if (!articleText.contains("\"")
                && !articleText.contains("'")) {
            issues.add("No quoted human references detected.");
            impact += 4;
        }

        // ===================================================
        // REPORT
        // ===================================================

        if (!issues.isEmpty()) {
            System.out.println("\n--- Content Analysis ---");
            for (String issue : issues) {
                System.out.println("CONTENT WARNING: " + issue);
            }
        }

        // ===================================================
        // FINAL RISK CLASSIFICATION
        // ===================================================

        return impact < 12 ? Risk.LOW :
                impact < 30 ? Risk.MEDIUM :
                        Risk.HIGH;
    }

    private static int detect(String text,
                              String[] phrases,
                              List<String> issues,
                              int weight,
                              String label) {

        int score = 0;

        for (String phrase : phrases) {
            if (text.contains(phrase)) {
                issues.add(label + ": " + phrase);
                score += weight;
            }
        }

        return score;
    }

    private static double calculateAverageSentenceLength(String text) {

        String[] sentences = text.split("[.!?]");
        int totalWords = text.split("\\s+").length;

        if (sentences.length == 0) return 0;

        return (double) totalWords / sentences.length;
    }
}
