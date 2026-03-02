package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.time.LocalDate;
import java.util.*;

public class MoviesStore {

    public final int MIN_YEAR = 1888;
    public final int MAX_YEAR;

    private final Map<Integer, Movie> catalog;

    // TODO: connect MoviesStoreJsonAdapter, add read() when first creating store & update() for each movie added/deleted
    public MoviesStore() {
        this.catalog = new HashMap<>();
        MAX_YEAR = LocalDate.now().getYear() + 1;
    }

    public List<Movie> getMovies() {
        return List.copyOf(catalog.values());
    }

    public List<Movie> getMoviesByYear(int year) {
        return catalog.values().stream().filter(movie -> movie.getYear() == year).toList();
    }

    public Optional<Movie> getMovieById(int id) {
        if (catalog.containsKey(id)) {
            return Optional.of(catalog.get(id));
        }
        return Optional.empty();
    }

    public Optional<Movie> getMovie(String title, int year) {
        Movie searchMovie = new Movie(title, year);
        return catalog.values().stream()
                .filter(movie -> movie.equals(searchMovie))
                .findFirst();
    }

    public boolean has(Movie movie) {
        return catalog.values().stream().anyMatch(item -> item.equals(movie));
    }

    public Movie addMovie(Movie movie) {
        assignId(movie);
        catalog.put(movie.getId(), movie);
        return movie;
    }

    public boolean delete(int id) {
        if (catalog.containsKey(id)) {
            catalog.remove(id);
            return true;
        }

        return false;
    }

    public void deleteAll() {
        catalog.clear();
    }

    private void assignId(Movie movie) {
        int nextId = catalog.size() + 1;
        movie.setId(nextId);
    }


}