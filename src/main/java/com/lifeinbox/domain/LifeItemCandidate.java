package com.lifeinbox.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LifeItemCandidate {
    private UUID id;
    private UUID inboxEntryId;
    private String title;
    private LifeItemType type;
    private String summary;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime deadline;
    private String location;
    private String suggestedAction;
    private boolean actionRequired;
    private double confidence;
    private List<String> missingInformation = new ArrayList<String>();
    private List<String> evidence = new ArrayList<String>();
    private LifeItemStatus status = LifeItemStatus.PENDING_CONFIRMATION;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LifeItemType getType() {
        return type;
    }

    public void setType(LifeItemType type) {
        this.type = type;
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

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(boolean actionRequired) {
        this.actionRequired = actionRequired;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getMissingInformation() {
        return missingInformation;
    }

    public void setMissingInformation(List<String> missingInformation) {
        this.missingInformation = missingInformation;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }

    public LifeItemStatus getStatus() {
        return status;
    }

    public void setStatus(LifeItemStatus status) {
        this.status = status;
    }
}
