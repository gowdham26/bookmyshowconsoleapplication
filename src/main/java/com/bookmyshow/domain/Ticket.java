package com.bookmyshow.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Ticket(String bookingId, Movie movie, Theatre theatre, Screen screen, LocalDateTime showTime,
                     List<Seat> seats, Customer customer, BigDecimal totalPrice) {
    public Ticket {
        bookingId = Movie.requireText(bookingId, "bookingId");
        Objects.requireNonNull(movie, "movie must not be null");
        Objects.requireNonNull(theatre, "theatre must not be null");
        Objects.requireNonNull(screen, "screen must not be null");
        if (!theatre.screens().contains(screen)) {
            throw new IllegalArgumentException("screen must belong to the ticket theatre");
        }
        Objects.requireNonNull(showTime, "showTime must not be null");
        Objects.requireNonNull(seats, "seats must not be null");
        seats = List.copyOf(seats);
        Objects.requireNonNull(customer, "customer must not be null");
        Objects.requireNonNull(totalPrice, "totalPrice must not be null");
    }

    public String getDetails() {
        String seatLabels = seats.stream().map(Seat::getLabel).collect(java.util.stream.Collectors.joining(", "));
        return "Ticket %s: %s at %s, %s, %s, seats %s, total %s".formatted(
                bookingId, movie.title(), theatre.name(), screen.name(), showTime, seatLabels, totalPrice);
    }
}
