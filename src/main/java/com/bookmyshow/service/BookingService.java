package com.bookmyshow.service;

import com.bookmyshow.domain.Booking;
import com.bookmyshow.domain.Customer;
import com.bookmyshow.domain.Screen;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.ShowSeatInventory;
import com.bookmyshow.repository.BookingRepository;
import com.bookmyshow.repository.CatalogRepository;
import com.bookmyshow.repository.ShowSeatInventoryRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class BookingService {
    private final CatalogRepository catalogRepository;
    private final ShowSeatInventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    public BookingService(CatalogRepository catalogRepository,
                          ShowSeatInventoryRepository inventoryRepository,
                          BookingRepository bookingRepository) {
        this(catalogRepository, inventoryRepository, bookingRepository, Clock.systemDefaultZone());
    }

    public BookingService(CatalogRepository catalogRepository,
                          ShowSeatInventoryRepository inventoryRepository,
                          BookingRepository bookingRepository,
                          Clock clock) {
        this.catalogRepository = Objects.requireNonNull(catalogRepository, "catalogRepository");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository");
        this.bookingRepository = Objects.requireNonNull(bookingRepository, "bookingRepository");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Booking bookSeats(Customer customer, String showId, List<String> seatIds) {
        if (customer == null) {
            throw new BookingException(BookingException.Reason.INVALID_CUSTOMER,
                    "Customer details are required");
        }
        List<String> selectedSeatIds = validateSelection(seatIds);
        Show show = catalogRepository.findShowById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));
        Screen screen = catalogRepository.findScreenById(show.screenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + show.screenId()));
        ShowSeatInventory inventory = inventoryRepository.findByShowId(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat inventory not found for show: " + showId));

        Set<String> physicalSeatIds = new HashSet<>();
        screen.seats().forEach(seat -> physicalSeatIds.add(seat.id()));
        if (!physicalSeatIds.containsAll(selectedSeatIds)) {
            throw new BookingException(BookingException.Reason.SEAT_NOT_ON_SCREEN,
                    "One or more selected seats do not belong to this show's screen");
        }

        synchronized (inventory) {
            if (!inventory.reserveAll(selectedSeatIds)) {
                throw new BookingException(BookingException.Reason.SEAT_UNAVAILABLE,
                        "One or more selected seats are no longer available");
            }
            BigDecimal totalPrice = show.pricePerSeat().multiply(BigDecimal.valueOf(selectedSeatIds.size()));
            Booking booking = new Booking(UUID.randomUUID().toString(), customer, showId,
                    selectedSeatIds, totalPrice, LocalDateTime.now(clock));
            try {
                bookingRepository.save(booking);
                return booking;
            } catch (RuntimeException exception) {
                inventory.releaseAll(selectedSeatIds);
                throw exception;
            }
        }
    }

    private static List<String> validateSelection(List<String> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new BookingException(BookingException.Reason.INVALID_SELECTION,
                    "Select at least one seat");
        }
        java.util.ArrayList<String> selected = new java.util.ArrayList<>();
        for (String seatId : seatIds) {
            if (seatId == null || seatId.isBlank()) {
                throw new BookingException(BookingException.Reason.INVALID_SELECTION,
                        "Selected seat IDs must not be blank");
            }
            selected.add(seatId.trim());
        }
        if (new HashSet<>(selected).size() != selected.size()) {
            throw new BookingException(BookingException.Reason.DUPLICATE_SEAT,
                    "A seat cannot be selected more than once");
        }
        return selected;
    }
}
