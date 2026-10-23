package com.bookmyshow.repository.memory;

import com.bookmyshow.domain.Movie;
import com.bookmyshow.domain.Screen;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.Theatre;
import com.bookmyshow.repository.CatalogRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryCatalogRepository implements CatalogRepository {
    private final Map<String, Movie> moviesById;
    private final Map<String, Show> showsById;
    private final Map<String, Screen> screensById;
    private final Map<String, Theatre> theatresByScreenId;

    public InMemoryCatalogRepository(Collection<Movie> movies, Collection<Theatre> theatres,
                                     Collection<Show> shows) {
        this.moviesById = indexById(movies, Movie::id, "movie");
        this.showsById = indexById(shows, Show::id, "show");
        Objects.requireNonNull(theatres, "theatres must not be null");

        Map<String, Screen> screens = new LinkedHashMap<>();
        Map<String, Theatre> screenTheatres = new LinkedHashMap<>();
        for (Theatre theatre : theatres) {
            for (Screen screen : theatre.screens()) {
                if (screens.putIfAbsent(screen.id(), screen) != null) {
                    throw new IllegalArgumentException("duplicate screen ID: " + screen.id());
                }
                screenTheatres.put(screen.id(), theatre);
            }
        }
        this.screensById = Map.copyOf(screens);
        this.theatresByScreenId = Map.copyOf(screenTheatres);

        for (Show show : this.showsById.values()) {
            if (!this.moviesById.containsKey(show.movieId())) {
                throw new IllegalArgumentException("show references unknown movie: " + show.movieId());
            }
            if (!this.screensById.containsKey(show.screenId())) {
                throw new IllegalArgumentException("show references unknown screen: " + show.screenId());
            }
        }
    }

    @Override
    public List<Movie> findMovies() {
        return moviesById.values().stream().sorted(Comparator.comparing(Movie::title)).toList();
    }

    @Override
    public Optional<Movie> findMovieById(String movieId) {
        return Optional.ofNullable(moviesById.get(movieId));
    }

    @Override
    public List<Show> findShows(String movieId, LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return showsById.values().stream()
                .filter(show -> show.movieId().equals(movieId)
                        && show.startTime().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Show::startTime).thenComparing(Show::id))
                .toList();
    }

    @Override
    public List<Show> findAllShows() {
        return showsById.values().stream()
                .sorted(Comparator.comparing(Show::startTime).thenComparing(Show::id))
                .toList();
    }

    @Override
    public Optional<Show> findShowById(String showId) {
        return Optional.ofNullable(showsById.get(showId));
    }

    @Override
    public Optional<Screen> findScreenById(String screenId) {
        return Optional.ofNullable(screensById.get(screenId));
    }

    @Override
    public Optional<Theatre> findTheatreByScreenId(String screenId) {
        return Optional.ofNullable(theatresByScreenId.get(screenId));
    }

    private static <T> Map<String, T> indexById(Collection<T> values,
                                                java.util.function.Function<T, String> idFunction,
                                                String type) {
        Objects.requireNonNull(values, type + "s must not be null");
        Map<String, T> indexed = new LinkedHashMap<>();
        for (T value : values) {
            Objects.requireNonNull(value, type + " must not be null");
            String id = idFunction.apply(value);
            if (indexed.putIfAbsent(id, value) != null) {
                throw new IllegalArgumentException("duplicate " + type + " ID: " + id);
            }
        }
        return Map.copyOf(indexed);
    }
}
