package com.truthtrace.demo.engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIExplainer {

    private static final String API_KEY = System.getenv("OPENAI_API_TT");

    public static String generateExplanation(
            String url,
            String overallRisk,
            String issuesSummary) {

        try {

            HttpClient client = HttpClient.newHttpClient();

            String prompt =
                    "A cybersecurity system analyzed the URL: " + url + ".\n" +
                            "Overall Risk Level: " + overallRisk + ".\n" +
                            "Detected signals:\n" +
                            issuesSummary + "\n\n" +
                            "Explain clearly why this risk level was assigned.";

            String escapedPrompt = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String requestBody = "{"
                    + "\"model\":\"gpt-4.1-mini\","
                    + "\"input\":\"" + escapedPrompt + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request,
                            HttpResponse.BodyHandlers.ofString());

            System.out.println("OpenAI Status: " + response.statusCode());

            if (response.statusCode() != 200) {
                System.out.println(response.body());
                return "AI explanation unavailable.";
            }

            return extractOutput(response.body());
        } catch (Exception e) {
            System.out.println("OpenAI exception: " + e.getMessage());
            return "AI explanation failed.";
        }
    }

    private static String extractOutput(String json) {

        String textMarker = "\"text\":";
        int textIndex = json.indexOf(textMarker);

        if (textIndex == -1) {
            return "No explanation returned.";
        }

        // Move to first quote after colon
        int startQuote = json.indexOf("\"", textIndex + textMarker.length());
        if (startQuote == -1) return "Parsing failed.";

        int start = startQuote + 1;

        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                result.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }

        return result.toString()
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("u2019", "'")
                .replace("nn", "\n\n")
                .replace("n ", "\n");

    }

}
