package com.bookmyshow.service;

public final class BookingException extends RuntimeException {
    public enum Reason {
        INVALID_CUSTOMER,
        INVALID_SELECTION,
        DUPLICATE_SEAT,
        SEAT_NOT_ON_SCREEN,
        SEAT_UNAVAILABLE
    }

    private final Reason reason;

    public BookingException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
