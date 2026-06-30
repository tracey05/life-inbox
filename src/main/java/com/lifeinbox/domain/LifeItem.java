package com.lifeinbox.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class LifeItem {
    private UUID id;
    private UUID inboxEntryId;
    private LifeItemType type;
    private String title;
    private String summary;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime deadline;
    private String location;
    private LifeItemStatus status;
    private Instant confirmedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInboxEntryId() {
        return inboxEntryId;
    }

    public void setInboxEntryId(UUID inboxEntryId) {
        this.inboxEntryId = inboxEntryId;
    }

    public LifeItemType getType() {
        return type;
    }

    public void setType(LifeItemType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LifeItemStatus getStatus() {
        return status;
    }

    public void setStatus(LifeItemStatus status) {
        this.status = status;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
