package com.truthtrace.demo.engine;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ArticleExtractionService {

    private static final String API_KEY =
            "68825fd13bmsheb649c59347f1fep1098f5jsnff6418b303f6";

    private static final String API_HOST =
            "text-extract7.p.rapidapi.com";

    private static final String BASE_URL =
            "https://text-extract7.p.rapidapi.com/?url=";

    public static String extractArticleText(String url) {

        try {

            HttpClient client = HttpClient.newHttpClient();

            String encodedUrl =
                    URLEncoder.encode(url, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + encodedUrl))
                    .header("X-RapidAPI-Key", API_KEY)
                    .header("X-RapidAPI-Host", API_HOST)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request,
                            HttpResponse.BodyHandlers.ofString());

            System.out.println("Extraction HTTP Status: "
                    + response.statusCode());

            if (response.statusCode() != 200) {
                System.out.println("Extraction Error Response: "
                        + response.body());
                return null;
            }

            String body = response.body();

            // Extract main text
            String text = extractJsonField(body, "text");

            if (text == null || text.length() < 50) {
                System.out.println("Extracted content too short or empty.");
                return null;
            }

            // Optional: extract title if available
            String title = extractJsonField(body, "title");

            if (title != null && !title.isBlank()) {
                text = title + ". " + text;
            }

            // Normalize spacing
            text = normalizeText(text);

            return text;

        } catch (Exception e) {
            System.out.println("Extraction failed: "
                    + e.getMessage());
            return null;
        }
    }

    private static String extractJsonField(String json,
                                           String fieldName) {

        String search = "\"" + fieldName + "\"";
        int index = json.indexOf(search);

        if (index == -1) return null;

        int colon = json.indexOf(":", index);
        if (colon == -1) return null;

        int startQuote = json.indexOf("\"", colon + 1);
        if (startQuote == -1) return null;

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }

        return sb.toString()
                .replace("\\n", " ")
                .replace("\\t", " ")
                .replace("\\r", " ")
                .replace("\\\"", "\"")
                .trim();
    }

    private static String normalizeText(String text) {

        // Collapse multiple spaces
        text = text.replaceAll("\\s+", " ");

        // Remove strange control characters
        text = text.replaceAll("[^\\x20-\\x7E]", "");

        return text.trim();
    }
}
