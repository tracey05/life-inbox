package com.lifeinbox.infrastructure.repository;

import com.lifeinbox.domain.InboxEntry;
import com.lifeinbox.domain.LifeItem;
import com.lifeinbox.domain.LifeItemCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryLifeInboxStore implements InboxEntryRepository, LifeItemCandidateRepository, LifeItemRepository {
    private final ConcurrentMap<UUID, InboxEntry> entries = new ConcurrentHashMap<UUID, InboxEntry>();
    private final ConcurrentMap<UUID, LifeItemCandidate> candidates = new ConcurrentHashMap<UUID, LifeItemCandidate>();
    private final ConcurrentMap<UUID, LifeItem> items = new ConcurrentHashMap<UUID, LifeItem>();

    @Override
    public void save(InboxEntry entry) {
        entries.put(entry.getId(), entry);
    }

    @Override
    public Optional<InboxEntry> findEntryById(UUID id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public void save(LifeItemCandidate candidate) {
        candidates.put(candidate.getId(), candidate);
    }

    @Override
    public void saveAll(List<LifeItemCandidate> candidates) {
        for (LifeItemCandidate candidate : candidates) {
            save(candidate);
        }
    }

    @Override
    public Optional<LifeItemCandidate> findByEntryIdAndCandidateId(UUID entryId, UUID candidateId) {
        LifeItemCandidate candidate = candidates.get(candidateId);
        if (candidate == null || !entryId.equals(candidate.getInboxEntryId())) {
            return Optional.empty();
        }
        return Optional.of(candidate);
    }

    @Override
    public List<LifeItemCandidate> findByEntryId(UUID entryId) {
        List<LifeItemCandidate> result = new ArrayList<LifeItemCandidate>();
        for (LifeItemCandidate candidate : candidates.values()) {
            if (entryId.equals(candidate.getInboxEntryId())) {
                result.add(candidate);
            }
        }
        return result;
    }

    @Override
    public void save(LifeItem item) {
        items.put(item.getId(), item);
    }

    @Override
    public Optional<LifeItem> findItemById(UUID id) {
        return Optional.ofNullable(items.get(id));
    }
}
