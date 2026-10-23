package com.bookmyshow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookmyshow.domain.Booking;
import com.bookmyshow.domain.Customer;
import com.bookmyshow.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class BookingServiceTest {
    private static final Customer CUSTOMER = new Customer("Pat", "pat@example.com");

    @Test
    void booksMultipleSeatsAndCalculatesTotalWithClockTime() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        Clock fixedClock = Clock.fixed(Instant.parse("2030-03-01T10:00:00Z"), ZoneOffset.UTC);
        BookingService service = new BookingService(fixture.data().catalogRepository(),
                fixture.data().inventoryRepository(), fixture.bookingRepository(), fixedClock);

        Booking booking = service.bookSeats(CUSTOMER, "SHOW-1", List.of("A1", "A2"));

        assertEquals(List.of("A1", "A2"), booking.seatIds());
        assertEquals(new BigDecimal("500.00"), booking.totalPrice());
        assertEquals(Instant.parse("2030-03-01T10:00:00Z"), booking.createdAt().toInstant(ZoneOffset.UTC));
        assertTrue(fixture.bookingRepository().findById(booking.id()).isPresent());
    }

    @Test
    void rejectsEmptyDuplicateAndOffScreenSelections() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        BookingService service = fixture.bookingService();

        assertEquals(BookingException.Reason.INVALID_SELECTION, assertThrows(BookingException.class,
                () -> service.bookSeats(CUSTOMER, "SHOW-1", List.of())).reason());
        assertEquals(BookingException.Reason.DUPLICATE_SEAT, assertThrows(BookingException.class,
                () -> service.bookSeats(CUSTOMER, "SHOW-1", List.of("A1", "A1"))).reason());
        assertEquals(BookingException.Reason.SEAT_NOT_ON_SCREEN, assertThrows(BookingException.class,
                () -> service.bookSeats(CUSTOMER, "SHOW-1", List.of("Z99"))).reason());
        assertEquals(0, fixture.bookingRepository().findByCustomerContact(CUSTOMER.contact()).size());
    }

    @Test
    void conflictingMultiSeatRequestDoesNotPartiallyReserveSeats() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        BookingService service = fixture.bookingService();
        service.bookSeats(CUSTOMER, "SHOW-1", List.of("A1"));

        assertEquals(BookingException.Reason.SEAT_UNAVAILABLE, assertThrows(BookingException.class,
                () -> service.bookSeats(CUSTOMER, "SHOW-1", List.of("A1", "A2"))).reason());
        assertTrue(fixture.data().inventoryRepository().findByShowId("SHOW-1")
                .orElseThrow().getAvailableSeatIds().contains("A2"));
    }

    @Test
    void sameSeatCanBeBookedForDifferentShows() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        BookingService service = fixture.bookingService();

        service.bookSeats(CUSTOMER, "SHOW-1", List.of("A1"));
        Booking secondShowBooking = service.bookSeats(CUSTOMER, "SHOW-2", List.of("A1"));

        assertEquals("SHOW-2", secondShowBooking.showId());
    }

    @Test
    void concurrentRequestsForSameSeatAllowOnlyOneBooking() throws Exception {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        BookingService service = fixture.bookingService();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> attemptBooking(service, ready, start));
            var second = executor.submit(() -> attemptBooking(service, ready, start));
            ready.await();
            start.countDown();

            int successful = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);
            assertEquals(1, successful);
            assertEquals(1, fixture.bookingRepository().findByCustomerContact(CUSTOMER.contact()).size());
            assertFalse(fixture.data().inventoryRepository().findByShowId("SHOW-1")
                    .orElseThrow().getAvailableSeatIds().contains("A1"));
        }
    }

    @Test
    void releasesSeatsIfSavingTheBookingFails() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        BookingRepository failingRepository = new BookingRepository() {
            @Override
            public void save(Booking booking) {
                throw new IllegalStateException("simulated storage failure");
            }

            @Override
            public java.util.Optional<Booking> findById(String bookingId) {
                return java.util.Optional.empty();
            }

            @Override
            public List<Booking> findByCustomerContact(String contact) {
                return List.of();
            }
        };
        BookingService service = new BookingService(fixture.data().catalogRepository(),
                fixture.data().inventoryRepository(), failingRepository);

        assertThrows(IllegalStateException.class,
                () -> service.bookSeats(CUSTOMER, "SHOW-1", List.of("A1")));
        assertTrue(fixture.data().inventoryRepository().findByShowId("SHOW-1")
                .orElseThrow().getAvailableSeatIds().contains("A1"));
    }

    @Test
    void rejectsUnknownShowAndMissingCustomer() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        BookingService service = fixture.bookingService();

        assertThrows(ResourceNotFoundException.class,
                () -> service.bookSeats(CUSTOMER, "missing", List.of("A1")));
        assertEquals(BookingException.Reason.INVALID_CUSTOMER, assertThrows(BookingException.class,
                () -> service.bookSeats(null, "SHOW-1", List.of("A1"))).reason());
    }

    private static boolean attemptBooking(BookingService service, CountDownLatch ready,
                                          CountDownLatch start) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            service.bookSeats(CUSTOMER, "SHOW-1", List.of("A1"));
            return true;
        } catch (BookingException exception) {
            if (exception.reason() != BookingException.Reason.SEAT_UNAVAILABLE) {
                throw exception;
            }
            return false;
        }
    }
}
