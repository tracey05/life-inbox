package com.lifeinbox.api;

import com.lifeinbox.api.dto.ResponseMapper;
import com.lifeinbox.application.CandidateConfirmationService;
import com.lifeinbox.application.InboxApplicationService;
import com.lifeinbox.application.SubmitEntryResult;
import com.lifeinbox.domain.LifeItem;
import com.lifeinbox.domain.LifeItemCandidate;
import com.lifeinbox.infrastructure.json.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class InboxController {
    private final InboxApplicationService inboxApplicationService;
    private final CandidateConfirmationService confirmationService;

    public InboxController(InboxApplicationService inboxApplicationService,
                           CandidateConfirmationService confirmationService) {
        this.inboxApplicationService = inboxApplicationService;
        this.confirmationService = confirmationService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        addCommonHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            send(exchange, 204, "");
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("POST".equals(method) && "/api/inbox/entries".equals(path)) {
                handleSubmit(exchange);
                return;
            }

            RouteMatch candidateRoute = matchCandidateRoute(path);
            if ("POST".equals(method) && candidateRoute != null && "confirm".equals(candidateRoute.action)) {
                handleConfirm(exchange, candidateRoute.entryId, candidateRoute.candidateId);
                return;
            }

            if ("POST".equals(method) && candidateRoute != null && "ignore".equals(candidateRoute.action)) {
                handleIgnore(exchange, candidateRoute.entryId, candidateRoute.candidateId);
                return;
            }

            sendError(exchange, 404, "Route not found");
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleSubmit(HttpExchange exchange) throws IOException {
        Map<String, Object> request = JsonUtil.parseObject(readBody(exchange));
        Object content = request.get("content");
        if (content == null || String.valueOf(content).trim().isEmpty()) {
            throw new IllegalArgumentException("content is required");
        }
        String sourceType = request.containsKey("sourceType") ? String.valueOf(request.get("sourceType")) : "MANUAL_TEXT";
        SubmitEntryResult result = inboxApplicationService.submit(sourceType, String.valueOf(content));
        sendJson(exchange, 200, ResponseMapper.submitEntryResponse(result));
    }

    private void handleConfirm(HttpExchange exchange, UUID entryId, UUID candidateId) throws IOException {
        Map<String, Object> overrides = JsonUtil.parseObject(readBody(exchange));
        LifeItem item = confirmationService.confirm(entryId, candidateId, overrides);
        sendJson(exchange, 200, ResponseMapper.confirmResponse(item));
    }

    private void handleIgnore(HttpExchange exchange, UUID entryId, UUID candidateId) throws IOException {
        LifeItemCandidate candidate = confirmationService.ignore(entryId, candidateId);
        sendJson(exchange, 200, ResponseMapper.ignoreResponse(candidate));
    }

    private RouteMatch matchCandidateRoute(String path) {
        String prefix = "/api/inbox/entries/";
        if (!path.startsWith(prefix)) {
            return null;
        }
        String rest = path.substring(prefix.length());
        String[] parts = rest.split("/");
        if (parts.length != 4 || !"candidates".equals(parts[1])) {
            return null;
        }
        return new RouteMatch(UUID.fromString(parts[0]), UUID.fromString(parts[2]), parts[3]);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        send(exchange, status, JsonUtil.stringify(body));
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("error", message);
        sendJson(exchange, status, body);
    }

    private void addCommonHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream output = exchange.getResponseBody();
        output.write(bytes);
        output.close();
    }

    private static class RouteMatch {
        private final UUID entryId;
        private final UUID candidateId;
        private final String action;

        private RouteMatch(UUID entryId, UUID candidateId, String action) {
            this.entryId = entryId;
            this.candidateId = candidateId;
            this.action = action;
        }
    }
}
