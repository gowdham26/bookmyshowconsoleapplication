package com.bookmyshow.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class TicketTest {
    @Test
    void ticketDetailsIncludeTheScreenName() {
        Movie movie = new Movie("MOV-1", "Sample Movie", "English", 120);
        Seat seat = new Seat("A1", "A", 1);
        Screen screen = new Screen("SCR-1", "Screen 1", List.of(seat));
        Theatre theatre = new Theatre("TH-1", "Sample Theatre", "Bengaluru", List.of(screen));
        Ticket ticket = new Ticket("BOOK-1", movie, theatre, screen,
                LocalDateTime.of(2030, 1, 1, 16, 0), List.of(seat),
                new Customer("Pat", "pat@example.com"), new BigDecimal("200.00"));

        assertTrue(ticket.getDetails().contains("Screen 1"));
    }

    @Test
    void rejectsAScreenThatDoesNotBelongToTheTheatre() {
        Screen theatreScreen = new Screen("SCR-1", "Screen 1", List.of(new Seat("A1", "A", 1)));
        Screen unrelatedScreen = new Screen("SCR-2", "Screen 2", List.of(new Seat("A1", "A", 1)));
        Theatre theatre = new Theatre("TH-1", "Sample Theatre", "Bengaluru", List.of(theatreScreen));

        assertThrows(IllegalArgumentException.class, () -> new Ticket("BOOK-1",
                new Movie("MOV-1", "Sample Movie", "English", 120), theatre, unrelatedScreen,
                LocalDateTime.of(2030, 1, 1, 16, 0), List.of(new Seat("A1", "A", 1)),
                new Customer("Pat", "pat@example.com"), new BigDecimal("200.00")));
    }
}
