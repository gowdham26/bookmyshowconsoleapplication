package com.bookmyshow.repository.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookmyshow.domain.Movie;
import com.bookmyshow.domain.Screen;
import com.bookmyshow.domain.Seat;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.Theatre;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryCatalogRepositoryTest {
    private static final LocalDate DATE = LocalDate.of(2030, 3, 12);

    @Test
    void findsMoviesAndShowsByMovieAndDateInStartTimeOrder() {
        Movie movie = new Movie("MOV-1", "Sample Movie", "English", 120);
        Screen screen = screen("SCR-1");
        Theatre theatre = new Theatre("TH-1", "Sample Theatre", "Bengaluru", List.of(screen));
        Show late = show("SHOW-LATE", movie.id(), screen.id(), DATE.atTime(LocalTime.of(20, 0)));
        Show early = show("SHOW-EARLY", movie.id(), screen.id(), DATE.atTime(LocalTime.of(16, 0)));
        Show nextDay = show("SHOW-NEXT", movie.id(), screen.id(), DATE.plusDays(1).atTime(16, 0));
        InMemoryCatalogRepository repository = new InMemoryCatalogRepository(
                List.of(movie), List.of(theatre), List.of(late, nextDay, early));

        assertEquals(List.of(movie), repository.findMovies());
        assertEquals(List.of(early, late), repository.findShows(movie.id(), DATE));
        assertEquals(theatre, repository.findTheatreByScreenId(screen.id()).orElseThrow());
    }

    @Test
    void rejectsShowWithUnknownMovieOrScreen() {
        Movie movie = new Movie("MOV-1", "Sample Movie", "English", 120);
        Screen screen = screen("SCR-1");
        Theatre theatre = new Theatre("TH-1", "Sample Theatre", "Bengaluru", List.of(screen));

        assertThrows(IllegalArgumentException.class, () -> new InMemoryCatalogRepository(
                List.of(movie), List.of(theatre), List.of(show("SHOW-1", "MISSING", "SCR-1",
                        DATE.atTime(16, 0)))));
        assertThrows(IllegalArgumentException.class, () -> new InMemoryCatalogRepository(
                List.of(movie), List.of(theatre), List.of(show("SHOW-1", "MOV-1", "MISSING",
                        DATE.atTime(16, 0)))));
    }

    private static Screen screen(String id) {
        return new Screen(id, "Screen 1", List.of(new Seat("A1", "A", 1)));
    }

    private static Show show(String id, String movieId, String screenId,
                             java.time.LocalDateTime startTime) {
        return new Show(id, movieId, screenId, startTime, new BigDecimal("200.00"));
    }
}
