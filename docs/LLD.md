# Movie Ticket Booking Application — LLD

This document records the approved v1 class design. Catalog and booking data are in memory and reset when the application exits.

## Domain classes

| Class | Key attributes | Methods and responsibility |
| --- | --- | --- |
| `Movie` | `id`, `title`, `language`, `durationMinutes` | `getDetails()` — provide movie details for display. |
| `Theatre` | `id`, `name`, `city`, `screens` | `getScreens()` — provide the theatre's screens. |
| `Screen` | `id`, `name`, `seats` | `getSeatsByRow()` — group physical seats for console display. |
| `Seat` | `id`, `row`, `number` | `getLabel()` — return a label such as `A10`. Seat identity does not contain booking status. |
| `Show` | `id`, `movieId`, `screenId`, `startTime`, `pricePerSeat` | `getDetails()` — provide the scheduled screening details. Each screening has a unique show ID. |
| `ShowSeatInventory` | `showId`, `allSeatIds`, `bookedSeatIds` | `getAvailableSeatIds()`, `getBookedSeatIds()`, `reserveAll(seatIds)`, `releaseAll(seatIds)` — track and atomically update availability for one show. |
| `Customer` | `name`, `contact` | `getDetails()` — provide customer details for booking and lookup. |
| `Booking` | `id`, `customer`, `showId`, `seatIds`, `totalPrice`, `createdAt` | `getDetails()` — provide the confirmed booking details. |
| `Ticket` | `bookingId`, `movie`, `theatre`, `screen`, `showTime`, `seats`, `customer`, `totalPrice` | `getDetails()` — provide the ticket view, including the screen name, for display or lookup. |

## Application services

| Class | Methods and responsibility |
| --- | --- |
| `CatalogService` | `listMovies()`, `findShows(movieId, date)`, `findShowOptions(movieId, date)`, `findShowDates(movieId)` — browse movies and screenings with theatre/screen details and available dates. |
| `AvailabilityService` | `getSeatAvailability(showId)` — return a show's seats and their availability. |
| `BookingService` | `bookSeats(customer, showId, seatIds)` — validate the selection, reserve all seats atomically, calculate the total, save and return the booking. |
| `BookingQueryService` | `findBookingsByContact(contact)`, `getTicket(bookingId)` — retrieve booking history or ticket details. |

`SeatAvailability` pairs a physical `Seat` with its availability for one show. `ShowOption` pairs a screening with its theatre and screen for display. `ResourceNotFoundException` identifies missing catalog, show, inventory, or booking data. `BookingException` carries a reason for invalid customer details or seat-selection failures.

## Repositories and console

| Class | Methods and responsibility |
| --- | --- |
| `CatalogRepository` | `findMovies()`, `findMovieById(movieId)`, `findShows(movieId, date)`, `findShowById(showId)`, `findScreenById(screenId)` — read catalog data. |
| `ShowSeatInventoryRepository` | `findByShowId(showId)`, `save(inventory)` — retrieve or store the independent inventory for a screening. |
| `BookingRepository` | `save(booking)`, `findById(bookingId)`, `findByCustomerContact(contact)` — store and retrieve bookings. |
| `ConsoleApp` | `run()` — prompt for movie, available date, and bookable show; render the seat map; book seats; show booking history; and retrieve tickets through application services. Input and output streams are injected for testing. |
| `Main` | `main(args)` — wire sample data, in-memory repositories, services, and start the console application. |

## Booking and availability rules

- A physical `Seat` belongs to a `Screen`; it has no global booked/available state.
- Every scheduled screening has a unique `Show.id` and its own `ShowSeatInventory`. The same seat can be booked independently for different shows, including shows at the same time on different screens.
- Booking confirmation rechecks availability. A per-show atomic reservation makes overlapping requests for the same seat resolve to one success and one unavailable-seat result. A multi-seat request reserves all requested seats or none.
- If saving a booking fails after seats are reserved, the booking service releases those seats before returning the error.
- The v1 in-memory implementation coordinates bookings within one running application process. It does not coordinate separate processes or machines.
