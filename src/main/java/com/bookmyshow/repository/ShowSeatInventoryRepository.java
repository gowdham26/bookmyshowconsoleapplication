package com.bookmyshow.repository;

import com.bookmyshow.domain.ShowSeatInventory;
import java.util.Optional;

public interface ShowSeatInventoryRepository {
    Optional<ShowSeatInventory> findByShowId(String showId);

    void save(ShowSeatInventory inventory);
}
