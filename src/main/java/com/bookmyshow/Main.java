package com.bookmyshow;

import com.bookmyshow.bootstrap.ApplicationData;
import com.bookmyshow.bootstrap.SampleDataFactory;
import com.bookmyshow.console.ConsoleApp;
import com.bookmyshow.repository.memory.InMemoryBookingRepository;
import com.bookmyshow.service.AvailabilityService;
import com.bookmyshow.service.BookingQueryService;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.service.CatalogService;

/** Application entry point that wires the in-memory application and starts the console. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        ApplicationData data = SampleDataFactory.createDefault();
        InMemoryBookingRepository bookingRepository = new InMemoryBookingRepository();
        CatalogService catalogService = new CatalogService(data.catalogRepository());
        AvailabilityService availabilityService = new AvailabilityService(
                data.catalogRepository(), data.inventoryRepository());
        BookingService bookingService = new BookingService(data.catalogRepository(),
                data.inventoryRepository(), bookingRepository);
        BookingQueryService bookingQueryService = new BookingQueryService(
                bookingRepository, data.catalogRepository());
        new ConsoleApp(System.in, System.out, catalogService, availabilityService,
                bookingService, bookingQueryService).run();
    }
}
