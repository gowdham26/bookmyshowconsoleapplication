package com.bookmyshow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {
    @Test
    void listsMoviesAndFindsShowsForMovieAndDate() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        CatalogService service = new CatalogService(fixture.data().catalogRepository());

        assertEquals(2, service.listMovies().size());
        assertEquals(2, service.findShows("MOV-1", ServiceFixtures.DATE).size());
        assertEquals(0, service.findShows("MOV-1", ServiceFixtures.DATE.plusDays(1)).size());
        assertEquals(List.of(ServiceFixtures.DATE), service.findShowDates("MOV-1"));
        assertEquals(List.of(ServiceFixtures.DATE), service.findShowDates("MOV-2"));
    }

    @Test
    void reportsUnknownMovie() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        CatalogService service = new CatalogService(fixture.data().catalogRepository());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findShows("missing", ServiceFixtures.DATE));
    }
}
