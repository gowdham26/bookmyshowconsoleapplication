package com.bookmyshow.bootstrap;

import com.bookmyshow.domain.Movie;
import com.bookmyshow.domain.Screen;
import com.bookmyshow.domain.Seat;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.ShowSeatInventory;
import com.bookmyshow.domain.Theatre;
import com.bookmyshow.repository.memory.InMemoryCatalogRepository;
import com.bookmyshow.repository.memory.InMemoryShowSeatInventoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class SampleDataFactory {
    private SampleDataFactory() {
    }

    public static ApplicationData createDefault() {
        return create(LocalDate.now().plusDays(1));
    }

    public static ApplicationData create(LocalDate showDate) {
        Movie dune = new Movie("MOV-1", "Dune: Part Two", "English", 166);
        Movie interstellar = new Movie("MOV-2", "Interstellar", "English", 169);

        List<Seat> firstScreenSeats = seats("A", "B", "C", "D");
        List<Seat> secondScreenSeats = seats("A", "B", "C", "D");
        Screen screenOne = new Screen("SCR-1", "Screen 1", firstScreenSeats);
        Screen screenTwo = new Screen("SCR-2", "Screen 2", secondScreenSeats);
        Theatre theatre = new Theatre("TH-1", "PVR Orion Mall", "Bengaluru",
                List.of(screenOne, screenTwo));

        Show showOne = new Show("SHOW-1", dune.id(), screenOne.id(),
                showDate.atTime(LocalTime.of(16, 0)), new BigDecimal("250.00"));
        Show showTwo = new Show("SHOW-2", dune.id(), screenTwo.id(),
                showDate.atTime(LocalTime.of(16, 0)), new BigDecimal("250.00"));
        Show showThree = new Show("SHOW-3", interstellar.id(), screenOne.id(),
                showDate.atTime(LocalTime.of(19, 30)), new BigDecimal("220.00"));

        InMemoryCatalogRepository catalogRepository = new InMemoryCatalogRepository(
                List.of(dune, interstellar), List.of(theatre), List.of(showOne, showTwo, showThree));
        InMemoryShowSeatInventoryRepository inventoryRepository =
                new InMemoryShowSeatInventoryRepository();
        for (Show show : catalogRepository.findAllShows()) {
            Screen screen = catalogRepository.findScreenById(show.screenId()).orElseThrow();
            inventoryRepository.save(new ShowSeatInventory(show.id(),
                    screen.seats().stream().map(Seat::id).toList()));
        }
        return new ApplicationData(catalogRepository, inventoryRepository);
    }

    private static List<Seat> seats(String... rows) {
        java.util.ArrayList<Seat> seats = new java.util.ArrayList<>();
        for (String row : rows) {
            for (int number = 1; number <= 5; number++) {
                seats.add(new Seat(row + number, row, number));
            }
        }
        return List.copyOf(seats);
    }
}
