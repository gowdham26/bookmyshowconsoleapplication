package com.bookmyshow.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Show(String id, String movieId, String screenId, LocalDateTime startTime,
                   BigDecimal pricePerSeat) {
    public Show {
        id = Movie.requireText(id, "id");
        movieId = Movie.requireText(movieId, "movieId");
        screenId = Movie.requireText(screenId, "screenId");
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(pricePerSeat, "pricePerSeat must not be null");
        if (pricePerSeat.signum() < 0) {
            throw new IllegalArgumentException("pricePerSeat must not be negative");
        }
    }

    public String getDetails() {
        return "Show %s at %s for %s per seat".formatted(id, startTime, pricePerSeat);
    }
}
