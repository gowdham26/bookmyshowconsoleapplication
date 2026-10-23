package com.bookmyshow.service;

import com.bookmyshow.domain.SeatAvailability;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.ShowSeatInventory;
import com.bookmyshow.domain.Screen;
import com.bookmyshow.repository.CatalogRepository;
import com.bookmyshow.repository.ShowSeatInventoryRepository;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class AvailabilityService {
    private final CatalogRepository catalogRepository;
    private final ShowSeatInventoryRepository inventoryRepository;

    public AvailabilityService(CatalogRepository catalogRepository,
                               ShowSeatInventoryRepository inventoryRepository) {
        this.catalogRepository = Objects.requireNonNull(catalogRepository, "catalogRepository");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository");
    }

    public List<SeatAvailability> getSeatAvailability(String showId) {
        Show show = catalogRepository.findShowById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));
        Screen screen = catalogRepository.findScreenById(show.screenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + show.screenId()));
        ShowSeatInventory inventory = inventoryRepository.findByShowId(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat inventory not found for show: " + showId));
        Set<String> availableSeatIds = inventory.getAvailableSeatIds();
        return screen.seats().stream()
                .map(seat -> new SeatAvailability(seat, availableSeatIds.contains(seat.id())))
                .toList();
    }
}
