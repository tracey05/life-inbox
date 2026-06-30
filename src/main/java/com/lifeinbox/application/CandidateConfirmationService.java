package com.lifeinbox.application;

import com.lifeinbox.domain.LifeItem;
import com.lifeinbox.domain.LifeItemCandidate;
import com.lifeinbox.domain.LifeItemStatus;
import com.lifeinbox.domain.LifeItemType;
import com.lifeinbox.infrastructure.repository.LifeItemCandidateRepository;
import com.lifeinbox.infrastructure.repository.LifeItemRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CandidateConfirmationService {
    private final LifeItemCandidateRepository candidateRepository;
    private final LifeItemRepository lifeItemRepository;

    public CandidateConfirmationService(LifeItemCandidateRepository candidateRepository,
                                        LifeItemRepository lifeItemRepository) {
        this.candidateRepository = candidateRepository;
        this.lifeItemRepository = lifeItemRepository;
    }

    public LifeItem confirm(UUID entryId, UUID candidateId, Map<String, Object> overrides) {
        LifeItemCandidate candidate = findCandidate(entryId, candidateId);
        applyOverrides(candidate, overrides);

        LifeItem item = new LifeItem();
        item.setId(UUID.randomUUID());
        item.setInboxEntryId(entryId);
        item.setType(candidate.getType());
        item.setTitle(candidate.getTitle());
        item.setSummary(candidate.getSummary());
        item.setStartTime(candidate.getStartTime());
        item.setEndTime(candidate.getEndTime());
        item.setDeadline(candidate.getDeadline());
        item.setLocation(candidate.getLocation());
        item.setStatus(LifeItemStatus.CONFIRMED);
        item.setConfirmedAt(Instant.now());

        candidate.setStatus(LifeItemStatus.CONFIRMED);
        candidateRepository.save(candidate);
        lifeItemRepository.save(item);
        return item;
    }

    public LifeItemCandidate ignore(UUID entryId, UUID candidateId) {
        LifeItemCandidate candidate = findCandidate(entryId, candidateId);
        candidate.setStatus(LifeItemStatus.IGNORED);
        candidateRepository.save(candidate);
        return candidate;
    }

    private LifeItemCandidate findCandidate(UUID entryId, UUID candidateId) {
        Optional<LifeItemCandidate> candidate = candidateRepository.findByEntryIdAndCandidateId(entryId, candidateId);
        if (!candidate.isPresent()) {
            throw new IllegalArgumentException("Candidate not found for entry");
        }
        return candidate.get();
    }

    private void applyOverrides(LifeItemCandidate candidate, Map<String, Object> overrides) {
        if (overrides == null) {
            return;
        }
        if (overrides.containsKey("type")) {
            candidate.setType(LifeItemType.valueOf(String.valueOf(overrides.get("type"))));
        }
        if (overrides.containsKey("title")) {
            candidate.setTitle(nullableString(overrides.get("title")));
        }
        if (overrides.containsKey("summary")) {
            candidate.setSummary(nullableString(overrides.get("summary")));
        }
        if (overrides.containsKey("startTime")) {
            candidate.setStartTime(nullableDateTime(overrides.get("startTime")));
        }
        if (overrides.containsKey("endTime")) {
            candidate.setEndTime(nullableDateTime(overrides.get("endTime")));
        }
        if (overrides.containsKey("deadline")) {
            candidate.setDeadline(nullableDateTime(overrides.get("deadline")));
        }
        if (overrides.containsKey("location")) {
            candidate.setLocation(nullableString(overrides.get("location")));
        }
    }

    private String nullableString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime nullableDateTime(Object value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.parse(String.valueOf(value));
    }
}
