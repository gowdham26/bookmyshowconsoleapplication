package com.bookmyshow.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {
    @Test
    void reportsAvailabilitySeparatelyForEachShow() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        var firstShowInventory = fixture.data().inventoryRepository()
                .findByShowId("SHOW-1").orElseThrow();
        firstShowInventory.reserveAll(List.of("A1"));
        AvailabilityService service = fixture.availabilityService();

        assertFalse(service.getSeatAvailability("SHOW-1").stream()
                .filter(item -> item.seat().id().equals("A1"))
                .findFirst().orElseThrow().available());
        assertTrue(service.getSeatAvailability("SHOW-2").stream()
                .filter(item -> item.seat().id().equals("A1"))
                .findFirst().orElseThrow().available());
    }

    @Test
    void reportsUnknownShow() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();

        assertThrows(ResourceNotFoundException.class,
                () -> fixture.availabilityService().getSeatAvailability("missing"));
    }
}
