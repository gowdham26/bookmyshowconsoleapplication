package com.bookmyshow.console;

import com.bookmyshow.domain.Booking;
import com.bookmyshow.domain.Customer;
import com.bookmyshow.domain.Movie;
import com.bookmyshow.domain.SeatAvailability;
import com.bookmyshow.domain.Ticket;
import com.bookmyshow.service.AvailabilityService;
import com.bookmyshow.service.BookingException;
import com.bookmyshow.service.BookingQueryService;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.service.CatalogService;
import com.bookmyshow.service.ResourceNotFoundException;
import com.bookmyshow.service.ShowOption;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ConsoleApp {
    private final BufferedReader input;
    private final PrintWriter output;
    private final CatalogService catalogService;
    private final AvailabilityService availabilityService;
    private final BookingService bookingService;
    private final BookingQueryService bookingQueryService;

    public ConsoleApp(InputStream input, OutputStream output, CatalogService catalogService,
                      AvailabilityService availabilityService, BookingService bookingService,
                      BookingQueryService bookingQueryService) {
        this.input = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(input, "input"), StandardCharsets.UTF_8));
        this.output = new PrintWriter(Objects.requireNonNull(output, "output"), true,
                StandardCharsets.UTF_8);
        this.catalogService = Objects.requireNonNull(catalogService, "catalogService");
        this.availabilityService = Objects.requireNonNull(availabilityService, "availabilityService");
        this.bookingService = Objects.requireNonNull(bookingService, "bookingService");
        this.bookingQueryService = Objects.requireNonNull(bookingQueryService, "bookingQueryService");
    }

    public void run() {
        output.println("=== Movie Ticket Booking Application ===");
        while (true) {
            printMainMenu();
            String choice = readLine("Choose an option: ");
            if (choice == null) {
                output.println("Goodbye.");
                return;
            }
            switch (choice.trim()) {
                case "1" -> browseAndBook();
                case "2" -> showBookingHistory();
                case "3" -> showTicketById();
                case "0" -> {
                    output.println("Goodbye.");
                    return;
                }
                default -> output.println("Invalid option. Choose 1, 2, 3, or 0.");
            }
        }
    }

    private void printMainMenu() {
        output.println();
        output.println("1. Browse movies and book tickets");
        output.println("2. View bookings by customer contact");
        output.println("3. Retrieve ticket by booking ID");
        output.println("0. Exit");
    }

    private void browseAndBook() {
        List<Movie> movies = catalogService.listMovies();
        if (movies.isEmpty()) {
            output.println("No movies are currently available.");
            return;
        }
        output.println("Movies:");
        for (int index = 0; index < movies.size(); index++) {
            Movie movie = movies.get(index);
            output.printf("%d. %s%n", index + 1, movie.getDetails());
        }
        Integer movieChoice = readChoice("Select a movie (0 to return): ", movies.size());
        if (movieChoice == null || movieChoice == 0) {
            return;
        }

        Movie movie = movies.get(movieChoice - 1);
        Map<LocalDate, List<ShowOption>> showsByDate = new LinkedHashMap<>();
        for (LocalDate date : catalogService.findShowDates(movie.id())) {
            List<ShowOption> bookableShows = findBookableShows(movie.id(), date);
            if (!bookableShows.isEmpty()) {
                showsByDate.put(date, bookableShows);
            }
        }
        List<LocalDate> availableDates = List.copyOf(showsByDate.keySet());
        if (availableDates.isEmpty()) {
            output.println("No shows with available seats for this movie.");
            return;
        }
        output.println("Dates with available seats:");
        for (int index = 0; index < availableDates.size(); index++) {
            output.printf("%d. %s%n", index + 1, availableDates.get(index));
        }
        Integer dateChoice = readChoice("Select a date (0 to return): ", availableDates.size());
        if (dateChoice == null || dateChoice == 0) {
            return;
        }
        List<ShowOption> shows = showsByDate.get(availableDates.get(dateChoice - 1));
        output.println("Shows:");
        for (int index = 0; index < shows.size(); index++) {
            ShowOption option = shows.get(index);
            output.printf("%d. %s at %s, %s, %s, %s per seat%n",
                    index + 1, movie.title(), option.theatre().name(), option.screen().name(),
                    option.show().startTime(), option.show().pricePerSeat().toPlainString());
        }
        Integer showChoice = readChoice("Select a show (0 to return): ", shows.size());
        if (showChoice == null || showChoice == 0) {
            return;
        }
        ShowOption selectedShow = shows.get(showChoice - 1);
        List<SeatAvailability> seats;
        try {
            seats = availabilityService.getSeatAvailability(selectedShow.show().id());
        } catch (ResourceNotFoundException exception) {
            output.println(exception.getMessage());
            return;
        }
        displaySeatMap(seats);

        String seatSelection = readLine("Enter seat labels separated by commas (for example A1,A2): ");
        if (seatSelection == null) {
            return;
        }
        List<String> selectedSeatIds = parseSeatSelection(seatSelection, seats);
        if (selectedSeatIds == null) {
            return;
        }

        String customerName = readLine("Customer name: ");
        if (customerName == null) {
            return;
        }
        String contact = readLine("Customer email or phone: ");
        if (contact == null) {
            return;
        }
        try {
            Customer customer = new Customer(customerName, contact);
            Booking booking = bookingService.bookSeats(customer, selectedShow.show().id(), selectedSeatIds);
            Ticket ticket = bookingQueryService.getTicket(booking.id());
            output.println("Booking confirmed.");
            output.println(ticket.getDetails());
        } catch (BookingException | ResourceNotFoundException | IllegalArgumentException exception) {
            output.println("Booking failed: " + exception.getMessage());
        }
    }

    private List<ShowOption> findBookableShows(String movieId, LocalDate date) {
        return catalogService.findShowOptions(movieId, date).stream()
                .filter(option -> availabilityService.getSeatAvailability(option.show().id()).stream()
                        .anyMatch(SeatAvailability::available))
                .toList();
    }

    private void displaySeatMap(List<SeatAvailability> seats) {
        Map<String, List<SeatAvailability>> rows = new java.util.TreeMap<>();
        for (SeatAvailability seat : seats) {
            rows.computeIfAbsent(seat.seat().row(), ignored -> new ArrayList<>()).add(seat);
        }
        output.println("Seat map (O = available, X = booked):");
        rows.forEach((row, rowSeats) -> {
            rowSeats.sort(java.util.Comparator.comparingInt(item -> item.seat().number()));
            output.print(row + ": ");
            for (SeatAvailability item : rowSeats) {
                output.printf("%d[%s] ", item.seat().number(), item.available() ? "O" : "X");
            }
            output.println();
        });
    }

    private List<String> parseSeatSelection(String selection, List<SeatAvailability> seats) {
        if (selection.isBlank()) {
            output.println("Booking failed: Select at least one seat.");
            return null;
        }
        Map<String, String> idsByLabel = new HashMap<>();
        seats.forEach(item -> idsByLabel.put(item.seat().getLabel().toUpperCase(), item.seat().id()));
        List<String> seatIds = new ArrayList<>();
        for (String token : selection.split(",", -1)) {
            String label = token.trim().toUpperCase();
            String seatId = idsByLabel.get(label);
            if (seatId == null) {
                output.println("Booking failed: Unknown seat " + token.trim() + ".");
                return null;
            }
            seatIds.add(seatId);
        }
        return List.copyOf(seatIds);
    }

    private void showBookingHistory() {
        String contact = readLine("Customer email or phone: ");
        if (contact == null) {
            return;
        }
        try {
            List<Booking> bookings = bookingQueryService.findBookingsByContact(contact);
            if (bookings.isEmpty()) {
                output.println("No bookings found for that contact.");
                return;
            }
            output.println("Bookings:");
            bookings.forEach(booking -> output.printf(
                    "%s | Show %s | Seats %s | Total %s%n", booking.id(), booking.showId(),
                    String.join(", ", booking.seatIds()), booking.totalPrice().toPlainString()));
        } catch (IllegalArgumentException exception) {
            output.println("Lookup failed: " + exception.getMessage());
        }
    }

    private void showTicketById() {
        String bookingId = readLine("Booking ID: ");
        if (bookingId == null) {
            return;
        }
        try {
            output.println(bookingQueryService.getTicket(bookingId.trim()).getDetails());
        } catch (ResourceNotFoundException exception) {
            output.println("Ticket lookup failed: " + exception.getMessage());
        }
    }

    private Integer readChoice(String prompt, int maximum) {
        String value = readLine(prompt);
        if (value == null) {
            return null;
        }
        try {
            int choice = Integer.parseInt(value.trim());
            if (choice >= 0 && choice <= maximum) {
                return choice;
            }
        } catch (NumberFormatException ignored) {
            // Report the invalid choice through the common message below.
        }
        output.println("Invalid selection.");
        return 0;
    }

    private String readLine(String prompt) {
        output.print(prompt);
        output.flush();
        try {
            return input.readLine();
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read console input", exception);
        }
    }
}
