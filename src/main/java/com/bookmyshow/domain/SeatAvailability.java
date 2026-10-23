package com.bookmyshow.domain;

import java.util.Objects;

public record SeatAvailability(Seat seat, boolean available) {
    public SeatAvailability {
        Objects.requireNonNull(seat, "seat must not be null");
    }
}
