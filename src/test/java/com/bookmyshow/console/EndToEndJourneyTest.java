package com.bookmyshow.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookmyshow.bootstrap.ApplicationData;
import com.bookmyshow.bootstrap.SampleDataFactory;
import com.bookmyshow.repository.memory.InMemoryBookingRepository;
import com.bookmyshow.service.AvailabilityService;
import com.bookmyshow.service.BookingQueryService;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.service.CatalogService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class EndToEndJourneyTest {
    @Test
    void customerCanBrowseBookAndRetrieveTheIssuedTicket() {
        JourneyApp app = new JourneyApp();

        String bookingOutput = app.run("1\n1\n1\n2\nA1,A2\nPat\npat@example.com\n0\n");
        var booking = app.bookings.findByCustomerContact("pat@example.com").getFirst();
        String lookupOutput = app.run("2\npat@example.com\n3\n" + booking.id() + "\n0\n");

        assertTrue(bookingOutput.contains("Dune: Part Two at PVR Orion Mall, Screen 2"));
        assertTrue(bookingOutput.contains("Booking confirmed."));
        assertEquals("SHOW-2", booking.showId());
        assertEquals(List.of("A1", "A2"), booking.seatIds());
        assertTrue(lookupOutput.contains(booking.id()));
        assertTrue(lookupOutput.contains("PVR Orion Mall, Screen 2"));
        assertTrue(lookupOutput.contains("seats A1, A2, total 500.00"));
    }

    @Test
    void sameSeatCanBeBookedThroughTheConsoleForTwoDifferentShows() {
        JourneyApp app = new JourneyApp();

        String firstOutput = app.run("1\n1\n1\n1\nA1\nPat\npat@example.com\n0\n");
        String secondOutput = app.run("1\n1\n1\n2\nA1\nPat\npat@example.com\n0\n");
        var bookings = app.bookings.findByCustomerContact("pat@example.com");

        assertTrue(firstOutput.contains("Booking confirmed."));
        assertTrue(secondOutput.contains("Booking confirmed."));
        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().anyMatch(booking -> booking.showId().equals("SHOW-1")));
        assertTrue(bookings.stream().anyMatch(booking -> booking.showId().equals("SHOW-2")));
        assertTrue(app.data.inventoryRepository().findByShowId("SHOW-1")
                .orElseThrow().getBookedSeatIds().contains("A1"));
        assertTrue(app.data.inventoryRepository().findByShowId("SHOW-2")
                .orElseThrow().getBookedSeatIds().contains("A1"));
    }

    private static final class JourneyApp {
        private final ApplicationData data = SampleDataFactory.create(LocalDate.of(2030, 3, 12));
        private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
        private final CatalogService catalog = new CatalogService(data.catalogRepository());
        private final AvailabilityService availability = new AvailabilityService(
                data.catalogRepository(), data.inventoryRepository());
        private final BookingService booking = new BookingService(
                data.catalogRepository(), data.inventoryRepository(), bookings);
        private final BookingQueryService query = new BookingQueryService(
                bookings, data.catalogRepository());

        private String run(String scriptedInput) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ConsoleApp console = new ConsoleApp(
                    new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)), output,
                    catalog, availability, booking, query);
            console.run();
            return output.toString(StandardCharsets.UTF_8);
        }
    }
}
