package com.bookmyshow.service;

import com.bookmyshow.domain.Booking;
import com.bookmyshow.domain.Movie;
import com.bookmyshow.domain.Screen;
import com.bookmyshow.domain.Seat;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.Theatre;
import com.bookmyshow.domain.Ticket;
import com.bookmyshow.repository.BookingRepository;
import com.bookmyshow.repository.CatalogRepository;
import java.util.List;
import java.util.Objects;

public final class BookingQueryService {
    private final BookingRepository bookingRepository;
    private final CatalogRepository catalogRepository;

    public BookingQueryService(BookingRepository bookingRepository, CatalogRepository catalogRepository) {
        this.bookingRepository = Objects.requireNonNull(bookingRepository, "bookingRepository");
        this.catalogRepository = Objects.requireNonNull(catalogRepository, "catalogRepository");
    }

    public List<Booking> findBookingsByContact(String contact) {
        if (contact == null || contact.isBlank()) {
            throw new IllegalArgumentException("contact must not be blank");
        }
        return bookingRepository.findByCustomerContact(contact.trim());
    }

    public Ticket getTicket(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        Show show = catalogRepository.findShowById(booking.showId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + booking.showId()));
        Movie movie = catalogRepository.findMovieById(show.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + show.movieId()));
        Screen screen = catalogRepository.findScreenById(show.screenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + show.screenId()));
        Theatre theatre = catalogRepository.findTheatreByScreenId(show.screenId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found for screen: " + show.screenId()));
        List<Seat> seats = booking.seatIds().stream()
                .map(seatId -> screen.seats().stream()
                        .filter(seat -> seat.id().equals(seatId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Booking references unknown seat " + seatId)))
                .toList();
        return new Ticket(booking.id(), movie, theatre, screen, show.startTime(), seats,
                booking.customer(), booking.totalPrice());
    }
}
