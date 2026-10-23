package com.bookmyshow.repository.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookmyshow.domain.Booking;
import com.bookmyshow.domain.Customer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryBookingRepositoryTest {
    @Test
    void retrievesBookingsByIdAndCustomerContactNewestFirst() {
        InMemoryBookingRepository repository = new InMemoryBookingRepository();
        Customer customer = new Customer("Pat", "pat@example.com");
        Booking older = booking("BOOK-1", customer, LocalDateTime.of(2030, 1, 1, 12, 0));
        Booking newer = booking("BOOK-2", customer, LocalDateTime.of(2030, 1, 2, 12, 0));
        repository.save(older);
        repository.save(newer);

        assertEquals(older, repository.findById(older.id()).orElseThrow());
        assertEquals(List.of(newer, older), repository.findByCustomerContact(customer.contact()));
        assertEquals(List.of(), repository.findByCustomerContact("missing@example.com"));
    }

    @Test
    void rejectsDuplicateBookingIds() {
        InMemoryBookingRepository repository = new InMemoryBookingRepository();
        Booking booking = booking("BOOK-1", new Customer("Pat", "pat@example.com"),
                LocalDateTime.of(2030, 1, 1, 12, 0));
        repository.save(booking);

        assertThrows(IllegalArgumentException.class, () -> repository.save(booking));
    }

    private static Booking booking(String id, Customer customer, LocalDateTime createdAt) {
        return new Booking(id, customer, "SHOW-1", List.of("A1"), new BigDecimal("200.00"), createdAt);
    }
}
