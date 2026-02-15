package com.truthtrace.demo.engine;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DomainAnalyzer {

    public static List<String> analyze(String url) {

        List<String> issues = new ArrayList<>();

        // -------------------------------
        // HTTPS CHECK
        // -------------------------------
        if (!url.startsWith("https://")) {
            issues.add("URL is not using HTTPS.");
        }

        String lowerUrl = url.toLowerCase();

        // -------------------------------
        // SUSPICIOUS KEYWORD DETECTION
        // -------------------------------
        String[] suspiciousKeywords = {
                "login", "verify", "secure",
                "update", "account", "bank",
                "validate", "reset-password",
                "verification", "secure-login",
                "auth", "confirm", "crypto", "giveaway",
                "bonus", "reward", "free-money", "invest", "airdrop",
                "hidden-secret", "breaking-news", "top-10", "miracle-cure",
                "ultimate-guide", "leaked", "act-now", "limited-time",
                "before-deleted", "urgent-alert", "immediate-action",
                "account-suspended", "security-warning"
        };

        for (String keyword : suspiciousKeywords) {
            if (lowerUrl.contains(keyword)) {
                issues.add("Suspicious keyword detected: " + keyword);
            }
        }

        // -------------------------------
        // DOMAIN STRUCTURE CHECK
        // -------------------------------
        URI uri = URI.create(url);
        String domain = uri.getHost();

        if (domain == null) {
            issues.add("Invalid URL format.");
            return issues;
        }

        domain = domain.toLowerCase();

        // Remove www.
        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }

        // Hyphen abuse
        int hyphenCount = domain.split("-").length - 1;
        if (hyphenCount >= 3) {
            issues.add("Domain contains excessive hyphens.");
        }

        // Numeric abuse
        int digitCount = domain.replaceAll("\\D", "").length();
        if (digitCount > 4) {
            issues.add("Domain contains excessive numeric characters.");
        }

        // Long domain
        if (domain.length() > 40) {
            issues.add("Unusually long domain detected.");
        }

        // -------------------------------
        // SUSPICIOUS TLD CHECK
        // -------------------------------
        String[] suspiciousTlds = {
                ".xyz", ".top", ".click", ".gq", ".ml",
                ".cf", ".tk", ".buzz", ".work", ".support"
        };

        for (String tld : suspiciousTlds) {
            if (domain.endsWith(tld)) {
                issues.add("Suspicious top-level domain detected: " + tld);
            }
        }

        // -------------------------------
        // SUBDOMAIN DEPTH CHECK
        // -------------------------------
        int subdomainDepth = domain.split("\\.").length - 2;
        if (subdomainDepth >= 2) {
            issues.add("Excessive subdomain depth detected.");
        }

        // -------------------------------
        // DOMAIN AGE (WHOIS API)
        // -------------------------------
        int age = DomainAgeChecker.checkDomainAgeInDays(domain);

        if (age == -1) {
            issues.add("Could not determine domain age.");
        } else if (age < 7) {
            issues.add("EXTREME RISK: Domain registered within 7 days.");
        } else if (age < 30) {
            issues.add("HIGH RISK: Domain registered within 30 days.");
        } else if (age < 90) {
            issues.add("Moderate risk: Domain less than 3 months old.");
        } else if (age < 365) {
            issues.add("Relatively new domain (less than 1 year old).");
        }

        // --------------------------------------
        // VirusTotal Reputation Check
        // --------------------------------------

        int vtDetections = VirusTotalChecker.checkUrlReputation(url);

        if (vtDetections > 0) {
            System.out.println("VirusTotal flagged this URL by "
                    + vtDetections + " security vendors.");
        } else {
            System.out.println("VirusTotal reports no malicious detections.");
        }


        return issues;
    }
}
