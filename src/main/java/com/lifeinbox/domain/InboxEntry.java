package com.lifeinbox.domain;

import java.time.Instant;
import java.util.UUID;

public class InboxEntry {
    private UUID id;
    private String sourceType;
    private String rawContent;
    private Instant createdAt;
    private ParseStatus parseStatus;

    public InboxEntry(UUID id, String sourceType, String rawContent, Instant createdAt, ParseStatus parseStatus) {
        this.id = id;
        this.sourceType = sourceType;
        this.rawContent = rawContent;
        this.createdAt = createdAt;
        this.parseStatus = parseStatus;
    }

    public UUID getId() {
        return id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getRawContent() {
        return rawContent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ParseStatus getParseStatus() {
        return parseStatus;
    }

    public void setParseStatus(ParseStatus parseStatus) {
        this.parseStatus = parseStatus;
    }
}
