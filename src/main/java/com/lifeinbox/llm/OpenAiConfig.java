package com.lifeinbox.llm;

import java.time.ZoneId;

public class OpenAiConfig {
    private static final String DEFAULT_MODEL = "gpt-4.1-mini";
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final ZoneId userZone;

    public OpenAiConfig(String apiKey, String model, String baseUrl, ZoneId userZone) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.userZone = userZone;
    }

    public static OpenAiConfig fromEnvironment() {
        String apiKey = trimToNull(System.getenv("OPENAI_API_KEY"));
        String model = defaultIfBlank(System.getenv("OPENAI_MODEL"), DEFAULT_MODEL);
        String baseUrl = defaultIfBlank(System.getenv("OPENAI_BASE_URL"), DEFAULT_BASE_URL);
        String timezone = defaultIfBlank(System.getenv("LIFE_INBOX_TIMEZONE"), ZoneId.systemDefault().getId());
        return new OpenAiConfig(apiKey, model, baseUrl, ZoneId.of(timezone));
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ZoneId getUserZone() {
        return userZone;
    }

    public String responsesUrl() {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized + "/responses";
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return trimmed.isEmpty() ? null : trimmed;
    }
}
