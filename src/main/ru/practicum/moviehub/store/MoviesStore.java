package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.time.LocalDate;
import java.util.*;

public class MoviesStore {

    private final int minYear = 1888;
    private final Map<Integer, Movie> catalog;

    private int nextId;

    // TODO: connect MoviesStoreJsonAdapter, add read() when first creating store & update() for each movie added/deleted
    public MoviesStore() {
        this.catalog = new HashMap<>();
        nextId = 1;
    }

    public boolean has(Movie movie) {
        return catalog.values().stream().anyMatch(item -> item.equals(movie));
    }

    // GET

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

    public int getMinYear() {
        return minYear;
    }

    public int getMaxYear() {
        return LocalDate.now().getYear() + 1;
    }

    // ADD

    public Movie addMovie(Movie movie) {
        assignId(movie);
        catalog.put(movie.getId(), movie);
        return movie;
    }

    // DELETE

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


    // HELPER METHODS

    private void assignId(Movie movie) {
        movie.setId(nextId);
        nextId++;
    }


}