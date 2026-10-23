package com.bookmyshow.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Booking(String id, Customer customer, String showId, List<String> seatIds,
                      BigDecimal totalPrice, LocalDateTime createdAt) {
    public Booking {
        id = Movie.requireText(id, "id");
        Objects.requireNonNull(customer, "customer must not be null");
        showId = Movie.requireText(showId, "showId");
        Objects.requireNonNull(seatIds, "seatIds must not be null");
        seatIds = seatIds.stream().map(seatId -> Movie.requireText(seatId, "seatId")).toList();
        if (seatIds.isEmpty()) {
            throw new IllegalArgumentException("a booking must contain at least one seat");
        }
        if (seatIds.stream().distinct().count() != seatIds.size()) {
            throw new IllegalArgumentException("seat IDs in a booking must be unique");
        }
        Objects.requireNonNull(totalPrice, "totalPrice must not be null");
        if (totalPrice.signum() < 0) {
            throw new IllegalArgumentException("totalPrice must not be negative");
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public String getDetails() {
        return "Booking %s: show %s, seats %s, total %s".formatted(
                id, showId, String.join(", ", seatIds), totalPrice);
    }
}
