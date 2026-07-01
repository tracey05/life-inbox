package com.lifeinbox.llm;

import com.lifeinbox.infrastructure.json.JsonUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAiResponsesLlmProvider implements LlmProvider {
    private final OpenAiConfig config;

    public OpenAiResponsesLlmProvider(OpenAiConfig config) {
        this.config = config;
    }

    @Override
    public String complete(String prompt) {
        if (!config.isConfigured()) {
            throw new IllegalStateException("OPENAI_API_KEY is required to call OpenAI");
        }

        try {
            String responseBody = post(JsonUtil.stringify(requestBody(prompt)));
            return extractOutputText(JsonUtil.parseObject(responseBody));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new IllegalStateException("Failed to call OpenAI Responses API", e);
        }
    }

    private Map<String, Object> requestBody(String prompt) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("model", config.getModel());
        body.put("input", prompt);
        body.put("text", textFormat());
        return body;
    }

    private Map<String, Object> textFormat() {
        Map<String, Object> jsonSchema = new LinkedHashMap<String, Object>();
        jsonSchema.put("type", "json_schema");
        jsonSchema.put("name", "life_item_extraction");
        jsonSchema.put("strict", true);
        jsonSchema.put("schema", LifeItemExtractionJsonSchema.schema());

        Map<String, Object> text = new LinkedHashMap<String, Object>();
        text.put("format", jsonSchema);
        return text;
    }

    private String post(String requestJson) throws IOException {
        URL url = new URL(config.responsesUrl());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
        connection.setRequestProperty("Content-Type", "application/json");

        byte[] requestBytes = requestJson.getBytes(StandardCharsets.UTF_8);
        OutputStream output = connection.getOutputStream();
        output.write(requestBytes);
        output.close();

        int status = connection.getResponseCode();
        String responseBody = readAll(status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream());
        if (status < 200 || status >= 300) {
            throw new IOException("OpenAI API returned " + status + ": " + responseBody);
        }
        return responseBody;
    }

    @SuppressWarnings("unchecked")
    private String extractOutputText(Map<String, Object> response) {
        Object directOutputText = response.get("output_text");
        if (directOutputText instanceof String) {
            return (String) directOutputText;
        }

        Object output = response.get("output");
        if (output instanceof List) {
            for (Object outputItem : (List<Object>) output) {
                if (!(outputItem instanceof Map)) {
                    continue;
                }
                Object content = ((Map<String, Object>) outputItem).get("content");
                if (!(content instanceof List)) {
                    continue;
                }
                for (Object contentItem : (List<Object>) content) {
                    if (contentItem instanceof Map) {
                        Object text = ((Map<String, Object>) contentItem).get("text");
                        if (text instanceof String) {
                            return (String) text;
                        }
                    }
                }
            }
        }

        throw new IllegalStateException("OpenAI response did not contain output text");
    }

    private String readAll(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
