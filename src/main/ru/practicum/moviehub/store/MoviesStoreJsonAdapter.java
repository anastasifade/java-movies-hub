package ru.practicum.moviehub.store;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.practicum.moviehub.model.Movie;

import java.io.*;
import java.util.List;

public class MoviesStoreJsonAdapter {

    private final Gson gson;
    private final String path;

    public MoviesStoreJsonAdapter(String path) throws FileNotFoundException {
        this.gson = new Gson();
        this.path = path;
    }

    // TODO: add json reader
    /*
    public List<Movie> read() throws IOException {
        try(FileReader reader = new FileReader(path)) {

        }
    } */

    // TODO: add json writer


}
