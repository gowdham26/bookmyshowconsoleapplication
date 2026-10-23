package com.bookmyshow.domain;

import java.util.Objects;

public record Movie(String id, String title, String language, int durationMinutes) {
    public Movie {
        id = requireText(id, "id");
        title = requireText(title, "title");
        language = requireText(language, "language");
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive");
        }
    }

    public String getDetails() {
        return "%s (%s, %d min)".formatted(title, language, durationMinutes);
    }

    static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return trimmed;
    }
}
