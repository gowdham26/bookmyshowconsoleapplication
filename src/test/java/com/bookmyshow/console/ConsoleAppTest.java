package com.bookmyshow.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookmyshow.bootstrap.ApplicationData;
import com.bookmyshow.bootstrap.SampleDataFactory;
import com.bookmyshow.domain.Customer;
import com.bookmyshow.repository.memory.InMemoryBookingRepository;
import com.bookmyshow.service.AvailabilityService;
import com.bookmyshow.service.BookingQueryService;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.service.CatalogService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ConsoleAppTest {
    @Test
    void completesBrowseAndMultiSeatBookingJourney() {
        TestApp app = new TestApp("1\n1\n1\n1\nA1,A2\nPat\npat@example.com\n0\n");

        app.console().run();

        String output = app.output();
        assertTrue(output.contains("Dates with available seats:"));
        assertTrue(output.contains("1. 2030-03-12"));
        assertTrue(output.contains("Screen 1"));
        assertTrue(output.contains("Screen 2"));
        assertTrue(output.contains("Seat map (O = available, X = booked)"));
        assertTrue(output.contains("Booking confirmed."));
        assertTrue(output.contains("seats A1, A2, total 500.00"));
        assertEquals(1, app.bookingRepository().findByCustomerContact("pat@example.com").size());
    }

    @Test
    void showsHistoryAndTicketLookupDetails() {
        TestApp app = new TestApp("0\n");
        var booking = app.bookingService().bookSeats(new Customer("Pat", "pat@example.com"),
                "SHOW-1", java.util.List.of("A1"));
        TestApp lookup = new TestApp("2\npat@example.com\n3\n" + booking.id() + "\n0\n",
                app.data(), app.bookingRepository());

        lookup.console().run();

        String output = lookup.output();
        assertTrue(output.contains(booking.id()));
        assertTrue(output.contains("PVR Orion Mall"));
        assertTrue(output.contains("Screen 1"));
        assertTrue(output.contains("A1"));
    }

    @Test
    void reportsInvalidMenuAndSeatInputWithoutCreatingBookings() {
        TestApp app = new TestApp("bad\n1\n1\n1\n1\nZ99\n0\n");

        app.console().run();

        String output = app.output();
        assertTrue(output.contains("Invalid option."));
        assertTrue(output.contains("Unknown seat Z99."));
        assertEquals(0, app.bookingRepository().findByCustomerContact("pat@example.com").size());
    }

    @Test
    void hidesSoldOutShowsFromTheShowList() {
        TestApp app = new TestApp("0\n");
        var soldOutInventory = app.data().inventoryRepository().findByShowId("SHOW-1").orElseThrow();
        soldOutInventory.reserveAll(soldOutInventory.getAvailableSeatIds());
        TestApp booking = new TestApp("1\n1\n1\n1\nA1\nPat\npat@example.com\n0\n",
                app.data(), app.bookingRepository());

        booking.console().run();

        assertTrue(booking.output().contains("PVR Orion Mall, Screen 2"));
        assertTrue(!booking.output().contains("PVR Orion Mall, Screen 1"));
        assertEquals("SHOW-2", booking.bookingRepository()
                .findByCustomerContact("pat@example.com").getFirst().showId());
    }

    @Test
    void omitsDatesWhenEveryShowForTheMovieIsSoldOut() {
        TestApp setup = new TestApp("0\n");
        for (String showId : java.util.List.of("SHOW-1", "SHOW-2")) {
            var inventory = setup.data().inventoryRepository().findByShowId(showId).orElseThrow();
            inventory.reserveAll(inventory.getAvailableSeatIds());
        }
        TestApp app = new TestApp("1\n1\n0\n", setup.data(), setup.bookingRepository());

        app.console().run();

        assertTrue(app.output().contains("No shows with available seats for this movie."));
        assertTrue(!app.output().contains("Dates with available seats:"));
    }

    private static final class TestApp {
        private final ApplicationData data;
        private final InMemoryBookingRepository bookingRepository;
        private final BookingService bookingService;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final ConsoleApp console;

        private TestApp(String input) {
            this(input, SampleDataFactory.create(LocalDate.of(2030, 3, 12)),
                    new InMemoryBookingRepository());
        }

        private TestApp(String input, ApplicationData data, InMemoryBookingRepository bookingRepository) {
            this.data = data;
            this.bookingRepository = bookingRepository;
            CatalogService catalogService = new CatalogService(data.catalogRepository());
            AvailabilityService availabilityService = new AvailabilityService(
                    data.catalogRepository(), data.inventoryRepository());
            this.bookingService = new BookingService(data.catalogRepository(),
                    data.inventoryRepository(), bookingRepository);
            BookingQueryService queryService = new BookingQueryService(
                    bookingRepository, data.catalogRepository());
            this.console = new ConsoleApp(
                    new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), output,
                    catalogService, availabilityService, bookingService, queryService);
        }

        private ConsoleApp console() {
            return console;
        }

        private String output() {
            return output.toString(StandardCharsets.UTF_8);
        }

        private ApplicationData data() {
            return data;
        }

        private InMemoryBookingRepository bookingRepository() {
            return bookingRepository;
        }

        private BookingService bookingService() {
            return bookingService;
        }
    }
}
