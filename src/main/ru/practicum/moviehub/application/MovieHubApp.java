package ru.practicum.moviehub.application;

import ru.practicum.moviehub.api.gateways.MoviesGateway;
import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.http.handlers.MoviesHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieHubApp {

    private final MoviesServer server;
    private final MoviesStore store;

    private MovieHubApp() throws IOException {
        store = new MoviesStore();
        server = new MoviesServer(8080);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    public static void main(String[] args) {
        try {
            MovieHubApp app = new MovieHubApp();
            app.start();
        } catch (Exception e) {
            // TODO: add logging
        }
    }

    private void start() {
        try {
            addContexts();
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addContexts() {
        server.createContext("/movies", new MoviesHandler(new MoviesGateway(store)));
    }

}