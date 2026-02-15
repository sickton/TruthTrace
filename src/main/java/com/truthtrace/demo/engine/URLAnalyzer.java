package com.truthtrace.demo.engine;



import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class URLAnalyzer {

    public static Risk analyze(String url) {

        int score = 100;

        System.out.println("\n--- URL / Infrastructure Analysis ---");

        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<Void> response =
                    client.send(request, HttpResponse.BodyHandlers.discarding());

            int statusCode = response.statusCode();

            System.out.println("HTTP Status Code: " + statusCode);

            if (statusCode == 200) {
                System.out.println("URL reachable.");
            } else if (statusCode == 404) {
                System.out.println("WARNING: URL returned 404 (Not Found).");
                score -= 30;
            } else if (statusCode == 403) {
                System.out.println("WARNING: URL returned 403 (Forbidden).");
                score -= 15;
            } else if (statusCode >= 500) {
                System.out.println("Server error detected.");
                score -= 20;
            } else {
                System.out.println("Received unexpected status: " + statusCode);
                score -= 10;
            }

        } catch (Exception e) {
            System.out.println("ERROR: Could not reach URL.");
            score -= 40;
        }

        // --------------------------------------
        // Domain Structure + Keyword Analysis
        // --------------------------------------

        List<String> domainIssues = DomainAnalyzer.analyze(url);

        for (String issue : domainIssues) {
            System.out.println("WARNING: " + issue);
            score -= 8;   // Slightly softer deduction per issue
        }

        // --------------------------------------
        // Domain Age Scoring (Whois API)
        // --------------------------------------

        int age = DomainAgeChecker.returnDaysOfDomain(url);

        if (age == -1) {
            System.out.println("WARNING: Unable to verify domain age.");
            score -= 10;
        } else if (age <= 7) {
            System.out.println("CRITICAL: Domain registered within 7 days.");
            score -= 30;
        } else if (age <= 30) {
            System.out.println("HIGH RISK: Domain registered within 30 days.");
            score -= 15;
        } else if (age <= 90) {
            System.out.println("Moderate Risk: Domain less than 3 months old.");
            score -= 8;
        } else if (age <= 365) {
            System.out.println("Domain less than 1 year old.");
            score -= 3;
        } else {
            System.out.println("Domain has long registration history.");
        }

        // Clamp score
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        System.out.println("Infrastructure Trust Score: " + score);

        Risk riskLevel =
                score > 80 ? Risk.LOW :
                        score > 50 ? Risk.MEDIUM :
                                Risk.HIGH;

        return riskLevel;
    }
}
