package com.bookmyshow.repository.memory;

import com.bookmyshow.domain.Booking;
import com.bookmyshow.repository.BookingRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryBookingRepository implements BookingRepository {
    private final Map<String, Booking> bookingsById = new ConcurrentHashMap<>();

    @Override
    public void save(Booking booking) {
        Booking previous = bookingsById.putIfAbsent(booking.id(), booking);
        if (previous != null) {
            throw new IllegalArgumentException("booking ID already exists: " + booking.id());
        }
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookingsById.get(bookingId));
    }

    @Override
    public List<Booking> findByCustomerContact(String contact) {
        return bookingsById.values().stream()
                .filter(booking -> booking.customer().contact().equals(contact))
                .sorted(Comparator.comparing(Booking::createdAt).reversed()
                        .thenComparing(Booking::id))
                .toList();
    }
}
