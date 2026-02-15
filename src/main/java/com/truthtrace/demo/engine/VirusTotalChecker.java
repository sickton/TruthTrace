package com.truthtrace.demo.engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class VirusTotalChecker {

    private static final String API_KEY = "fa243c89c9ef9c78b4a52d3b53fbb68dd7ae2658f7cbd1eecc7d6c78e6a444d8";

    public static int checkUrlReputation(String url) {

        try {
            HttpClient client = HttpClient.newHttpClient();

            // VirusTotal requires URL ID (Base64 encoded without padding)
            String encodedUrl = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(url.getBytes());

            String vtUrl =
                    "https://www.virustotal.com/api/v3/urls/" + encodedUrl;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vtUrl))
                    .header("x-apikey", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("VirusTotal lookup failed.");
                return 0;
            }

            String body = response.body();

            return extractMaliciousCount(body);

        } catch (Exception e) {
            System.out.println("VirusTotal error: " + e.getMessage());
            return 0;
        }
    }

    private static int extractMaliciousCount(String json) {

        String search = "\"malicious\":";
        int index = json.indexOf(search);

        if (index == -1) return 0;

        int start = index + search.length();
        int end = start;

        while (end < json.length()
                && Character.isDigit(json.charAt(end))) {
            end++;
        }

        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
