package com.bookmyshow.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record Screen(String id, String name, List<Seat> seats) {
    public Screen {
        id = Movie.requireText(id, "id");
        name = Movie.requireText(name, "name");
        Objects.requireNonNull(seats, "seats must not be null");
        seats = List.copyOf(seats);
        if (seats.isEmpty()) {
            throw new IllegalArgumentException("a screen must contain at least one seat");
        }
        long uniqueIds = seats.stream().map(Seat::id).distinct().count();
        long uniqueLabels = seats.stream().map(Seat::getLabel).distinct().count();
        if (uniqueIds != seats.size() || uniqueLabels != seats.size()) {
            throw new IllegalArgumentException("seat IDs and labels must be unique within a screen");
        }
    }

    public Map<String, List<Seat>> getSeatsByRow() {
        Map<String, List<Seat>> grouped = new TreeMap<>();
        for (Seat seat : seats) {
            grouped.computeIfAbsent(seat.row(), ignored -> new ArrayList<>()).add(seat);
        }
        grouped.values().forEach(rowSeats -> rowSeats.sort(
                java.util.Comparator.comparingInt(Seat::number)));
        Map<String, List<Seat>> result = new TreeMap<>();
        grouped.forEach((row, rowSeats) -> result.put(row, List.copyOf(rowSeats)));
        return Collections.unmodifiableMap(result);
    }
}
