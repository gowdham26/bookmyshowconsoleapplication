package com.bookmyshow.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookmyshow.domain.Show;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SampleDataFactoryTest {
    @Test
    void createsIndependentShowsAtTheSameTimeAndInitializesTheirInventories() {
        LocalDate date = LocalDate.of(2030, 3, 12);
        ApplicationData data = SampleDataFactory.create(date);
        List<Show> sameTimeShows = data.catalogRepository().findShows("MOV-1", date);

        assertEquals(2, sameTimeShows.size());
        assertEquals(LocalTime.of(16, 0), sameTimeShows.getFirst().startTime().toLocalTime());
        assertEquals(sameTimeShows.getFirst().startTime(), sameTimeShows.getLast().startTime());
        assertNotEquals(sameTimeShows.getFirst().id(), sameTimeShows.getLast().id());
        assertNotEquals(sameTimeShows.getFirst().screenId(), sameTimeShows.getLast().screenId());

        String seatId = "A1";
        assertTrue(data.inventoryRepository().findByShowId(sameTimeShows.getFirst().id())
                .orElseThrow().reserveAll(List.of(seatId)));
        assertTrue(data.inventoryRepository().findByShowId(sameTimeShows.getLast().id())
                .orElseThrow().getAvailableSeatIds().contains(seatId));
    }
}
