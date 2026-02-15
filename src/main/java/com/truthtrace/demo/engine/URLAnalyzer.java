package com.truthtrace.demo.engine;



import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class URLAnalyzer {

    public static Risk analyze(String url) {

        int score = 100;

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

            if (statusCode == 200) {
                score -= 0;
            } else if (statusCode == 404) {
                score -= 30;
            } else if (statusCode == 403) {
                score -= 15;
            } else if (statusCode >= 500) {
                score -= 20;
            } else {
                score -= 10;
            }

        } catch (Exception e) {
            score -= 40;
        }

        // --------------------------------------
        // Domain Structure + Keyword Analysis
        // --------------------------------------

        List<String> domainIssues = DomainAnalyzer.analyze(url);

        for (String issue : domainIssues) {
            score -= 8;   // Slightly softer deduction per issue
        }

        // --------------------------------------
        // Domain Age Scoring (Whois API)
        // --------------------------------------

        int age = DomainAgeChecker.returnDaysOfDomain(url);

        if (age == -1) {
            score -= 10;
        } else if (age <= 7) {
            score -= 30;
        } else if (age <= 30) {
            score -= 15;
        } else if (age <= 90) {
            score -= 8;
        } else if (age <= 365) {
            score -= 3;
        }

        // Clamp score
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        Risk riskLevel =
                score > 80 ? Risk.LOW :
                        score > 50 ? Risk.MEDIUM :
                                Risk.HIGH;

        return riskLevel;
    }
}
