package com.truthtrace.demo.engine;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DomainAgeChecker {

    private static final String API_KEY = "b4ca73abb861459d975bcfbdb8548abe";

    public static int checkDomainAgeInDays(String domain) {

        try {
            HttpClient client = HttpClient.newHttpClient();

            String requestUrl =
                    "https://api.whoisfreaks.com/v1.0/whois"
                            + "?apiKey=" + API_KEY
                            + "&whois=live"
                            + "&domainName=" + domain;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return -1;
            }

            String body = response.body();

            // Try multiple possible date fields returned by WhoisFreaks
            String creationDate = extractDate(body, "\"create_date\"");
            if (creationDate == null)
                creationDate = extractDate(body, "\"created_date\"");
            if (creationDate == null)
                creationDate = extractDate(body, "\"creation_date\"");
            if (creationDate == null)
                creationDate = extractDate(body, "\"registered_date\"");

            if (creationDate == null || creationDate.length() < 10) {
                return -1;
            }

            // Handle timestamps like: 2023-05-11T12:33:00Z
            String datePart = creationDate.substring(0, 10);

            LocalDate created = LocalDate.parse(datePart);
            int days = (int) ChronoUnit.DAYS.between(created, LocalDate.now());

            return Math.max(days, 0);

        } catch (Exception e) {
            return -1;
        }
    }

    private static String extractDate(String body, String fieldName) {

        int index = body.indexOf(fieldName);
        if (index == -1) return null;

        int colonIndex = body.indexOf(":", index);
        if (colonIndex == -1) return null;

        int startQuote = body.indexOf("\"", colonIndex);
        if (startQuote == -1) return null;

        int endQuote = body.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;

        return body.substring(startQuote + 1, endQuote);
    }

    public static int returnDaysOfDomain(String url) {

        try {
            URI uri = new URI(url);
            String domain = uri.getHost();

            if (domain == null)
                return -1;

            // Remove www. if present
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }

            return checkDomainAgeInDays(domain);

        } catch (URISyntaxException e) {
            return -1;
        }
    }
}
