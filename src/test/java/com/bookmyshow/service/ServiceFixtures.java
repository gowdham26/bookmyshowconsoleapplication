package com.bookmyshow.service;

import com.bookmyshow.bootstrap.ApplicationData;
import com.bookmyshow.bootstrap.SampleDataFactory;
import com.bookmyshow.repository.memory.InMemoryBookingRepository;
import java.time.LocalDate;

final class ServiceFixtures {
    static final LocalDate DATE = LocalDate.of(2030, 3, 12);

    private ServiceFixtures() {
    }

    static Fixture create() {
        ApplicationData data = SampleDataFactory.create(DATE);
        return new Fixture(data, new InMemoryBookingRepository());
    }

    record Fixture(ApplicationData data, InMemoryBookingRepository bookingRepository) {
        BookingService bookingService() {
            return new BookingService(data.catalogRepository(), data.inventoryRepository(), bookingRepository);
        }

        AvailabilityService availabilityService() {
            return new AvailabilityService(data.catalogRepository(), data.inventoryRepository());
        }

        BookingQueryService bookingQueryService() {
            return new BookingQueryService(bookingRepository, data.catalogRepository());
        }
    }
}
