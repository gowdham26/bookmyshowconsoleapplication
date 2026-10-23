package com.bookmyshow.repository;

import com.bookmyshow.domain.Movie;
import com.bookmyshow.domain.Screen;
import com.bookmyshow.domain.Show;
import com.bookmyshow.domain.Theatre;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CatalogRepository {
    List<Movie> findMovies();

    Optional<Movie> findMovieById(String movieId);

    List<Show> findShows(String movieId, LocalDate date);

    List<Show> findAllShows();

    Optional<Show> findShowById(String showId);

    Optional<Screen> findScreenById(String screenId);

    Optional<Theatre> findTheatreByScreenId(String screenId);
}
