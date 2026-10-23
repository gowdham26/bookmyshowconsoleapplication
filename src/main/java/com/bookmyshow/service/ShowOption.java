package com.bookmyshow.service;

import com.bookmyshow.domain.Screen;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.Theatre;
import java.util.Objects;

public record ShowOption(Show show, Theatre theatre, Screen screen) {
    public ShowOption {
        Objects.requireNonNull(show, "show must not be null");
        Objects.requireNonNull(theatre, "theatre must not be null");
        Objects.requireNonNull(screen, "screen must not be null");
    }
}
