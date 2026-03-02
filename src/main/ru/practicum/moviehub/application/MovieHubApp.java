package ru.practicum.moviehub.application;

import ru.practicum.moviehub.api.gateways.MoviesGateway;
import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.http.handlers.MoviesHandler;
import ru.practicum.moviehub.logging.MoviesLogger;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MovieHubApp {

    private final MoviesServer server;
    private final MoviesStore store;
    private static MoviesLogger log;

    private MovieHubApp() throws IOException {
        store = new MoviesStore();
        server = new MoviesServer(8080, log);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    public static void main(String[] args) {
        try {
            log = new MoviesLogger();
            try {
                MovieHubApp app = new MovieHubApp();
                app.start();
            } catch (IOException e) {
                log.logError(e, "Необработанная ошибка. Работа сервера завершена.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Непредвиденная ошибка при запуске программы.");
            e.printStackTrace();
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
        server.createContext("/movies", new MoviesHandler(new MoviesGateway(store), log));
    }

}