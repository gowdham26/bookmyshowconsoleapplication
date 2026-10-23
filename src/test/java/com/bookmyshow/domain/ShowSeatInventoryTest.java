package com.bookmyshow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShowSeatInventoryTest {
    @Test
    void reservationIsAllOrNothingAndCanBeReleased() {
        ShowSeatInventory inventory = new ShowSeatInventory("SHOW-1", List.of("A1", "A2", "A3"));

        assertTrue(inventory.reserveAll(List.of("A1")));
        assertFalse(inventory.reserveAll(List.of("A2", "UNKNOWN")));
        assertEquals(Set.of("A1"), inventory.getBookedSeatIds());
        assertEquals(Set.of("A2", "A3"), inventory.getAvailableSeatIds());
        assertTrue(inventory.releaseAll(List.of("A1")));
        assertEquals(Set.of("A1", "A2", "A3"), inventory.getAvailableSeatIds());
    }

    @Test
    void samePhysicalSeatCanBeAvailableForAnotherShow() {
        ShowSeatInventory firstShow = new ShowSeatInventory("SHOW-1", List.of("A1", "A2"));
        ShowSeatInventory secondShow = new ShowSeatInventory("SHOW-2", List.of("A1", "A2"));

        assertTrue(firstShow.reserveAll(List.of("A1")));

        assertEquals(Set.of("A2"), firstShow.getAvailableSeatIds());
        assertEquals(Set.of("A1", "A2"), secondShow.getAvailableSeatIds());
    }

    @Test
    void cannotReserveAlreadyBookedOrEmptySelection() {
        ShowSeatInventory inventory = new ShowSeatInventory("SHOW-1", List.of("A1", "A2"));

        assertTrue(inventory.reserveAll(List.of("A1")));
        assertFalse(inventory.reserveAll(List.of("A1", "A2")));
        assertFalse(inventory.reserveAll(List.of()));
        assertFalse(inventory.reserveAll(List.of("A2", "A2")));
        assertEquals(Set.of("A1"), inventory.getBookedSeatIds());
    }
}
