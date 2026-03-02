package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.http.handlers.BaseHttpHandler;
import ru.practicum.moviehub.logging.MoviesLogger;


import java.io.IOException;
import java.net.InetSocketAddress;


public class MoviesServer {

    private final HttpServer server;
    private final MoviesLogger log;
    private int port;

    public MoviesServer(int port, MoviesLogger log) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.log = log;
    }


    // Вспомогательные методы для создания и настройки сервера

    public void createContext(String context, BaseHttpHandler handler) {
        log.logInfo(String.format("Добавлен контекст [%s].", context));
        server.createContext(context, handler);
    }


    // Методы для запуска и остановки сервера

    public void start() {
        server.start();
        log.logInfo(String.format("Сервер запущен на %d порту.", port));
    }

    public void stop() {
        server.stop(0);
        log.logInfo("Сервер остановлен.");
    }

}