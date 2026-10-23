package com.bookmyshow.repository;

import com.bookmyshow.domain.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    void save(Booking booking);

    Optional<Booking> findById(String bookingId);

    List<Booking> findByCustomerContact(String contact);
}
