package com.bookmyshow.domain;

import java.util.List;
import java.util.Objects;

public record Theatre(String id, String name, String city, List<Screen> screens) {
    public Theatre {
        id = Movie.requireText(id, "id");
        name = Movie.requireText(name, "name");
        city = Movie.requireText(city, "city");
        Objects.requireNonNull(screens, "screens must not be null");
        screens = List.copyOf(screens);
        if (screens.isEmpty()) {
            throw new IllegalArgumentException("a theatre must contain at least one screen");
        }
        long uniqueIds = screens.stream().map(Screen::id).distinct().count();
        if (uniqueIds != screens.size()) {
            throw new IllegalArgumentException("screen IDs must be unique within a theatre");
        }
    }

    public List<Screen> getScreens() {
        return screens;
    }
}
