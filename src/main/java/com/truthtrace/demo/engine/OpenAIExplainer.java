package com.truthtrace.demo.engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


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
                            "Explain clearly why this risk level was assigned, and  make it fun and refer to the isuues raised." +
                            "Try making it sound like you're breaking down a crime scene";

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

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode outputArray = root.path("output");

            if (outputArray.isArray() && !outputArray.isEmpty()) {

                JsonNode firstMessage = outputArray.get(0);
                JsonNode contentArray = firstMessage.path("content");

                if (contentArray.isArray() && !contentArray.isEmpty()) {

                    JsonNode firstContent = contentArray.get(0);

                    return firstContent.path("text").asText();
                }
            }

            return "No explanation returned.";

        } catch (Exception e) {
            return "Failed to parse AI response.";
        }
    }
}
