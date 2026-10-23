package com.bookmyshow.repository.memory;

import com.bookmyshow.domain.ShowSeatInventory;
import com.bookmyshow.repository.ShowSeatInventoryRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryShowSeatInventoryRepository implements ShowSeatInventoryRepository {
    private final Map<String, ShowSeatInventory> inventories = new ConcurrentHashMap<>();

    @Override
    public Optional<ShowSeatInventory> findByShowId(String showId) {
        return Optional.ofNullable(inventories.get(showId));
    }

    @Override
    public void save(ShowSeatInventory inventory) {
        ShowSeatInventory previous = inventories.putIfAbsent(inventory.showId(), inventory);
        if (previous != null && previous != inventory) {
            throw new IllegalArgumentException("inventory already exists for show " + inventory.showId());
        }
    }
}
