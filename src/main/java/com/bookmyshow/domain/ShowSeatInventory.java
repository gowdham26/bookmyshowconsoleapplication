package com.bookmyshow.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Mutable availability state belonging to exactly one show. */
public final class ShowSeatInventory {
    private final String showId;
    private final Set<String> allSeatIds;
    private final Set<String> bookedSeatIds = new LinkedHashSet<>();

    public ShowSeatInventory(String showId, Collection<String> allSeatIds) {
        this.showId = Movie.requireText(showId, "showId");
        Objects.requireNonNull(allSeatIds, "allSeatIds must not be null");
        this.allSeatIds = new LinkedHashSet<>();
        for (String seatId : allSeatIds) {
            this.allSeatIds.add(Movie.requireText(seatId, "seatId"));
        }
        if (this.allSeatIds.isEmpty()) {
            throw new IllegalArgumentException("inventory must contain at least one seat");
        }
        if (this.allSeatIds.size() != allSeatIds.size()) {
            throw new IllegalArgumentException("seat IDs in an inventory must be unique");
        }
    }

    public String showId() {
        return showId;
    }

    public synchronized Set<String> getAvailableSeatIds() {
        Set<String> available = new LinkedHashSet<>(allSeatIds);
        available.removeAll(bookedSeatIds);
        return Collections.unmodifiableSet(available);
    }

    public synchronized Set<String> getBookedSeatIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(bookedSeatIds));
    }

    /** Reserves every requested seat, or leaves the inventory unchanged. */
    public synchronized boolean reserveAll(Collection<String> seatIds) {
        Set<String> requested = validateRequest(seatIds);
        if (requested.isEmpty() || requested.size() != seatIds.size()
                || !allSeatIds.containsAll(requested)
                || requested.stream().anyMatch(bookedSeatIds::contains)) {
            return false;
        }
        bookedSeatIds.addAll(requested);
        return true;
    }

    /** Releases every requested seat, or leaves the inventory unchanged. */
    public synchronized boolean releaseAll(Collection<String> seatIds) {
        Set<String> requested = validateRequest(seatIds);
        if (requested.isEmpty() || requested.size() != seatIds.size()
                || !allSeatIds.containsAll(requested)
                || !bookedSeatIds.containsAll(requested)) {
            return false;
        }
        bookedSeatIds.removeAll(requested);
        return true;
    }

    private static Set<String> validateRequest(Collection<String> seatIds) {
        Objects.requireNonNull(seatIds, "seatIds must not be null");
        Set<String> requested = new LinkedHashSet<>();
        for (String seatId : seatIds) {
            requested.add(Movie.requireText(seatId, "seatId"));
        }
        return requested;
    }
}
