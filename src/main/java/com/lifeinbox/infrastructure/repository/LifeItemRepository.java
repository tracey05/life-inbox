package com.lifeinbox.infrastructure.repository;

import com.lifeinbox.domain.LifeItem;

import java.util.Optional;
import java.util.UUID;

public interface LifeItemRepository {
    void save(LifeItem item);

    Optional<LifeItem> findItemById(UUID id);
}
