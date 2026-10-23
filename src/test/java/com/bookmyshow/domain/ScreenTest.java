package com.bookmyshow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScreenTest {
    @Test
    void groupsSeatsByRowAndSortsSeatNumbers() {
        Screen screen = new Screen("SCR-1", "Screen 1", List.of(
                new Seat("A10", "A", 10), new Seat("B2", "B", 2),
                new Seat("A2", "A", 2), new Seat("B1", "B", 1)));

        assertEquals(List.of("A2", "A10"), screen.getSeatsByRow().get("A").stream()
                .map(Seat::getLabel).toList());
        assertEquals(List.of("B1", "B2"), screen.getSeatsByRow().get("B").stream()
                .map(Seat::getLabel).toList());
    }

    @Test
    void rejectsDuplicatePhysicalSeatIds() {
        assertThrows(IllegalArgumentException.class, () -> new Screen("SCR-1", "Screen 1", List.of(
                new Seat("A1", "A", 1), new Seat("A1", "A", 2))));
    }

    @Test
    void rejectsDuplicateSeatLabelsEvenWhenIdsDiffer() {
        assertThrows(IllegalArgumentException.class, () -> new Screen("SCR-1", "Screen 1", List.of(
                new Seat("SEAT-1", "A", 1), new Seat("SEAT-2", "A", 1))));
    }
}
