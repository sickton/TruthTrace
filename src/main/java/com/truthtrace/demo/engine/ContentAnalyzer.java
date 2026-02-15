package com.truthtrace.demo.engine;

import java.util.ArrayList;
import java.util.List;

public class ContentAnalyzer {

    private static final int LOW_THRESHOLD = 12;
    private static final int HIGH_THRESHOLD = 30;

    public static Risk analyze(String articleText, List<String> pipeline) {

        if (articleText == null || articleText.isBlank()) {
            pipeline.add("No content extracted from target.");
            return Risk.MEDIUM;
        }

        String lower = articleText.toLowerCase();
        int impact = 0;

        pipeline.add("Content length: " + articleText.length() + " characters.");

        // ===================================================
        // 1️⃣ CONTENT DEPTH & STRUCTURE
        // ===================================================

        if (lower.length() < 300) {
            impact += 5;
            pipeline.add("Very short article content detected.");
        }

        int paragraphCount = articleText.split("\\n\\n").length;
        if (paragraphCount >= 5) {
            impact += 4;
            pipeline.add("Structured paragraph symmetry detected.");
        }

        double avgSentenceLength = calculateAverageSentenceLength(articleText);
        if (avgSentenceLength > 18 && avgSentenceLength < 23) {
            impact += 5;
            pipeline.add("Highly uniform sentence structure detected (possible AI pattern).");
        }

        if (!articleText.contains("\"") && !articleText.contains("'")) {
            impact += 4;
            pipeline.add("No quoted human references detected.");
        }

        // ===================================================
        // 2️⃣ SOCIAL ENGINEERING SIGNALS
        // ===================================================

        impact += detect(lower,
                new String[]{
                        "act now",
                        "urgent action required",
                        "limited time",
                        "final notice",
                        "immediate response"
                },
                pipeline, 4, "Urgency language detected");

        impact += detect(lower,
                new String[]{
                        "verify your account",
                        "confirm your identity",
                        "enter your password"
                },
                pipeline, 10, "Credential harvesting phrase detected");

        impact += detect(lower,
                new String[]{
                        "guaranteed return",
                        "double your investment",
                        "crypto giveaway",
                        "risk-free profit"
                },
                pipeline, 8, "Financial scam hook detected");

        impact += detect(lower,
                new String[]{
                        "shocking",
                        "you won't believe",
                        "what they don't want you to know",
                        "exposed"
                },
                pipeline, 5, "Emotional manipulation phrasing detected");

        int exclamations = articleText.length()
                - articleText.replace("!", "").length();

        if (exclamations > 5) {
            impact += 6;
            pipeline.add("Excessive exclamation marks detected.");
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
                pipeline, 3, "AI-generic phrasing detected");

        double lexicalDiversity = calculateLexicalDiversity(lower);
        if (lexicalDiversity < 0.35) {
            impact += 4;
            pipeline.add("Low lexical diversity detected (repetitive phrasing).");
        }

        // ===================================================
        // FINAL RISK CLASSIFICATION
        // ===================================================

        pipeline.add("Content impact score: " + impact);

        if (impact < LOW_THRESHOLD) return Risk.LOW;
        if (impact < HIGH_THRESHOLD) return Risk.MEDIUM;
        return Risk.HIGH;
    }

    // ===================================================
    // HELPER METHODS
    // ===================================================

    private static int detect(String text,
                              String[] phrases,
                              List<String> pipeline,
                              int weight,
                              String label) {

        int score = 0;

        for (String phrase : phrases) {
            if (text.contains(phrase)) {
                pipeline.add(label + " → \"" + phrase + "\"");
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

    private static double calculateLexicalDiversity(String text) {

        String[] words = text.split("\\s+");
        if (words.length == 0) return 0;

        List<String> unique = new ArrayList<>();

        for (String word : words) {
            String cleaned = word.replaceAll("[^a-z]", "");
            if (!cleaned.isBlank() && !unique.contains(cleaned)) {
                unique.add(cleaned);
            }
        }

        return (double) unique.size() / words.length;
    }
}
