package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.http.handlers.BaseHttpHandler;


import java.io.IOException;
import java.net.InetSocketAddress;


public class MoviesServer {

    private final HttpServer server;

    public MoviesServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
    }


    // Вспомогательные методы для создания и настройки сервера

    public void createContext(String context, BaseHttpHandler handler) {
        server.createContext(context, handler);
    }


    // Методы для запуска и остановки сервера

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

}