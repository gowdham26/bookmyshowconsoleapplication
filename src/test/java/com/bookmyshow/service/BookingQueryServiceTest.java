package com.bookmyshow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookmyshow.domain.Customer;
import org.junit.jupiter.api.Test;

class BookingQueryServiceTest {
    @Test
    void retrievesBookingHistoryAndTicketWithScreenDetails() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        var booking = fixture.bookingService().bookSeats(
                new Customer("Pat", "pat@example.com"), "SHOW-1", java.util.List.of("A1"));
        BookingQueryService service = fixture.bookingQueryService();

        assertEquals(1, service.findBookingsByContact(" pat@example.com ").size());
        var ticket = service.getTicket(booking.id());
        assertEquals("Screen 1", ticket.screen().name());
        assertTrue(ticket.getDetails().contains("Screen 1"));
    }

    @Test
    void reportsUnknownBookingAndRejectsBlankLookupContact() {
        ServiceFixtures.Fixture fixture = ServiceFixtures.create();
        BookingQueryService service = fixture.bookingQueryService();

        assertThrows(ResourceNotFoundException.class, () -> service.getTicket("missing"));
        assertThrows(IllegalArgumentException.class, () -> service.findBookingsByContact(" "));
    }
}
