package com.bookmyshow.domain;

public record Seat(String id, String row, int number) {
    public Seat {
        id = Movie.requireText(id, "id");
        row = Movie.requireText(row, "row");
        if (number <= 0) {
            throw new IllegalArgumentException("number must be positive");
        }
    }

    public String getLabel() {
        return row + number;
    }
}
