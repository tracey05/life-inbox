package com.lifeinbox.infrastructure.repository;

import com.lifeinbox.domain.LifeItemCandidate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LifeItemCandidateRepository {
    void save(LifeItemCandidate candidate);

    void saveAll(List<LifeItemCandidate> candidates);

    Optional<LifeItemCandidate> findByEntryIdAndCandidateId(UUID entryId, UUID candidateId);

    List<LifeItemCandidate> findByEntryId(UUID entryId);
}
