package com.lifeinbox.application;

import com.lifeinbox.domain.LifeItemCandidate;

import java.util.List;
import java.util.UUID;

public class SubmitEntryResult {
    private final UUID entryId;
    private final List<LifeItemCandidate> candidates;

    public SubmitEntryResult(UUID entryId, List<LifeItemCandidate> candidates) {
        this.entryId = entryId;
        this.candidates = candidates;
    }

    public UUID getEntryId() {
        return entryId;
    }

    public List<LifeItemCandidate> getCandidates() {
        return candidates;
    }
}
