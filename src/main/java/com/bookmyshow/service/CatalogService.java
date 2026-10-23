package com.bookmyshow.service;

import com.bookmyshow.domain.Movie;
import com.bookmyshow.domain.Show;
import com.bookmyshow.repository.CatalogRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CatalogService {
    private final CatalogRepository catalogRepository;

    public CatalogService(CatalogRepository catalogRepository) {
        this.catalogRepository = Objects.requireNonNull(catalogRepository, "catalogRepository");
    }

    public List<Movie> listMovies() {
        return catalogRepository.findMovies();
    }

    public List<Show> findShows(String movieId, LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        if (catalogRepository.findMovieById(movieId).isEmpty()) {
            throw new ResourceNotFoundException("Movie not found: " + movieId);
        }
        return catalogRepository.findShows(movieId, date);
    }

    public List<ShowOption> findShowOptions(String movieId, LocalDate date) {
        return findShows(movieId, date).stream()
                .map(show -> new ShowOption(show,
                        catalogRepository.findTheatreByScreenId(show.screenId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Theatre not found for screen: " + show.screenId())),
                        catalogRepository.findScreenById(show.screenId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Screen not found: " + show.screenId()))))
                .toList();
    }

    public List<LocalDate> findShowDates(String movieId) {
        if (catalogRepository.findMovieById(movieId).isEmpty()) {
            throw new ResourceNotFoundException("Movie not found: " + movieId);
        }
        return catalogRepository.findAllShows().stream()
                .filter(show -> show.movieId().equals(movieId))
                .map(show -> show.startTime().toLocalDate())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
