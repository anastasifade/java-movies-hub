package ru.practicum.moviehub.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.gateways.MoviesGateway;
import ru.practicum.moviehub.logging.MoviesLogger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;


public class MoviesHandler extends BaseHttpHandler {

    private final MoviesGateway gateway;
    private final MoviesLogger log;

    public MoviesHandler(MoviesGateway gateway, MoviesLogger log) {
        this.gateway = gateway;
        this.log = log;
    }


    // GET

    void handleGet(HttpExchange exchange) throws IOException {
        log.info("Принят запрос [GET]: " + exchange.getRequestURI());

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> headers = Map.of("Content-Type", C_TYPE_JSON);

        if (path.endsWith("movies")) {
            if (query == null) {
                handleGetAll(exchange, headers);
            } else {
                handleGetByQuery(exchange, headers, query);
            }
            return;
        }

        String[] pathElements = path.split("/");
        if (pathElements.length == 3) {
            handleGetById(exchange, headers, pathElements[2]);
            return;
        }

        log.warn(String.format("Запрос [GET]: [%s] не обработан - неподдерживаемый формат запроса",
                exchange.getRequestURI()));
        send(exchange, 400, headers, "Неподдерживаемый запрос.");
    }

    private void handleGetAll(HttpExchange exchange, Map<String, String> headers) throws IOException {
        processResponse(exchange, gateway.getAll(), 200, headers);
    }

    private void handleGetByQuery(HttpExchange exchange, Map<String, String> headers, String query) throws IOException {
        try {
            Map<String, String> params = queryToMap(query);
            if (params.containsKey("year") && params.size() == 1) {
                int year = Integer.parseInt(params.get("year"));
                processResponse(exchange, gateway.getByYear(year), 200, headers);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            log.warn(String.format("Ошибка при обработке запроса [GET]: [%s] - неподдерживаемый формат параметра запроса",
                    exchange.getRequestURI()));

            send(exchange, 400, headers, "Неподдерживаемый формат параметра запроса. " +
                    "Ожидиается запрос формата [year=XXXX], где XXXX - число от 1888 до " + LocalDate.now().getYear());
        }
    }

    private void handleGetById(HttpExchange exchange, Map<String, String> headers, String idAsStr) throws IOException {
        try {
            int id = Integer.parseInt(idAsStr);
            processResponse(exchange, gateway.getById(id), 200, headers);
        } catch (NumberFormatException e) {
            log.warn(String.format("Ошибка при обработке запроса [GET]: [%s] - " +
                            "неподдерживаемый формат уникального идентификатора", exchange.getRequestURI()));

            send(exchange, 400, headers, "Неподдерживаемый формат запроса. " +
                    "При поиске фильма по уникальному идентификатору ожидается запрос формата [/movies/XXXX], " +
                    "где XXXX - целое положительное число.");
        }
    }


    // POST

    void handlePost(HttpExchange exchange) throws IOException {
        log.info("Принят запрос [POST]: " + exchange.getRequestURI());

        if (!exchange.getRequestHeaders().get("Content-Type").contains(C_TYPE_JSON)) {
            log.warn(String.format("Ошибка при обработке запроса [POST]: [%s] - неподдерживаемые заголовки запроса.\n " +
                    "Заголовки: [%s]", exchange.getRequestURI(), exchange.getRequestHeaders().entrySet()));
            send(exchange, 415, Map.of("Content-Length", "0"));
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> headers = Map.of("Content-Type", C_TYPE_JSON);

        try {
            if (!path.endsWith("/movies") || query != null) {
                throw new IllegalArgumentException("Неподдерживаемый формат запроса");
            }

            String jsonRequest = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET).toLowerCase();

            if (jsonRequest.isEmpty()) {
                throw new InvalidPropertiesFormatException("Тело запроса не может быть пустым");
            }

            String title = parseFromJson(jsonRequest, "title");
            String year = parseFromJson(jsonRequest, "year");
            Map<String, String> params = Map.of("title", title, "year", year);
            processResponse(exchange, gateway.post(params), 201, headers);

        } catch (IllegalArgumentException e) {
            log.warn(String.format("Ошибка при обработке запроса [POST]: [%s] - %s",
                    exchange.getRequestURI(), e.getMessage().toLowerCase()));
            send(exchange, 400, headers, e.getMessage());
        } catch (InvalidPropertiesFormatException e) {
            log.warn(String.format("Ошибка при обработке запроса [POST]: [%s] - %s",
                    exchange.getRequestURI(), e.getMessage().toLowerCase()));
            send(exchange, 422, headers, e.getMessage());
        }
    }


    // DELETE

    void handleDelete(HttpExchange exchange) throws IOException {
        log.info("Принят запрос [DELETE]: " + exchange.getRequestURI());

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> headers = Map.of("Content-Length", "0");

        if (path.endsWith("movies") && query == null) {
            handleDeleteAll(exchange, headers);
            return;
        }

        String[] pathElements = path.split("/");
        if (pathElements.length == 3) {
            handleDeleteById(exchange, headers, pathElements[2]);
            return;
        }

        log.warn(String.format("Запрос [DELETE]: [%s] не обработан - неподдерживаемый формат запроса",
                exchange.getRequestURI()));
        send(exchange, 400, headers, "Неподдерживаемый запрос.");
    }

    private void handleDeleteAll(HttpExchange exchange, Map<String, String> headers) throws IOException {
        processResponse(exchange, gateway.deleteAll(), 204, headers);
    }

    private void handleDeleteById(HttpExchange exchange, Map<String, String> headers, String idAsStr) throws IOException {
        try {
            int id = Integer.parseInt(idAsStr);
            processResponse(exchange, gateway.deleteById(id), 204, headers);
        } catch (NumberFormatException e) {
            log.warn(String.format("Ошибка при обработке запроса [DELETE]: [%s] - " +
                    "неподдерживаемый формат уникального идентификатора", exchange.getRequestURI()));

            send(exchange, 400, headers, "Неподдерживаемый формат запроса. " +
                    "При удалении фильма по уникальному идентификатору ожидается запрос формата [/movies/XXXX], " +
                    "где XXXX - целое положительное число.");
        }
    }

}
