package com.lifeinbox.infrastructure.repository;

import com.lifeinbox.domain.InboxEntry;

import java.util.Optional;
import java.util.UUID;

public interface InboxEntryRepository {
    void save(InboxEntry entry);

    Optional<InboxEntry> findEntryById(UUID id);
}
