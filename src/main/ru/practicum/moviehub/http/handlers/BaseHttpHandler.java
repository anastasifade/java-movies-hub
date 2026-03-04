package ru.practicum.moviehub.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.response.ApiResponse;
import ru.practicum.moviehub.api.enums.FailureState;
import ru.practicum.moviehub.http.util.Method;
import ru.practicum.moviehub.http.util.StatusCodes;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseHttpHandler implements HttpHandler {

    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    static final String C_TYPE_JSON = "application/json; charset=UTF-8";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (getMethod(exchange.getRequestMethod())) {
                case GET -> handleGet(exchange);
                case POST -> handlePost(exchange);
                case DELETE -> handleDelete(exchange);
                case UNKNOWN -> send(exchange, StatusCodes.METHOD_NOT_ALLOWED, Map.of("Content-Length", "0"));
            }
        } catch (IOException e) {
            send(exchange, StatusCodes.SERVICE_UNAVAILABLE, Map.of("Content-Length", "0"));
        }
    }

    abstract void handleGet(HttpExchange exchange) throws IOException;

    abstract void handlePost(HttpExchange exchange) throws IOException;

    abstract void handleDelete(HttpExchange exchange) throws IOException;

    <T> void processResponse(HttpExchange exchange, ApiResponse<T> resp,
                             int defaultStatusCode, Map<String, String> headers) throws IOException {
        int sCode = defaultStatusCode;
        if (!resp.isSuccess()) {
            sCode = getErrorCode(resp.getFailureState().orElse(FailureState.UNKNOWN));
        }

        if (resp.getBody().isPresent()) {
            String body = serialize(resp.getBody().get());
            send(exchange, sCode, headers, body);
        } else {
            send(exchange, sCode, Map.of("Content-Length", "0"));
        }
    }


    // отправка ответа на запрос

    void send(HttpExchange ex, int statusCode, Map<String, String> headers, String body) throws IOException {
        try (OutputStream os = ex.getResponseBody()) {
            headers.keySet().forEach(key -> ex.getResponseHeaders().put(key, List.of(headers.get(key))));
            byte[] resp = body.getBytes(DEFAULT_CHARSET);
            ex.sendResponseHeaders(statusCode, resp.length);
            os.write(resp);
        }
    }

    void send(HttpExchange ex, int statusCode, Map<String, String> headers) throws IOException {
        headers.keySet().forEach(key -> ex.getResponseHeaders().set(key, headers.get(key)));
        ex.sendResponseHeaders(statusCode, -1);
        ex.close();
    }


    // вспомогательные методы

    Method getMethod(String method) {
        try {
            return Method.valueOf(method);
        } catch (IllegalArgumentException e) {
            return Method.UNKNOWN;
        }
    }

    int getErrorCode(FailureState state) {
        switch (state) {
            case INVALID_REQUEST -> {
                return StatusCodes.BAD_REQUEST;
            }
            case ITEM_NOT_FOUND -> {
                return StatusCodes.NOT_FOUND;
            }
            case INVALID_BODY -> {
                return StatusCodes.UNPROCESSABLE_CONTENT;
            }
            case ITEM_ALREADY_EXISTS -> {
                return StatusCodes.CONFLICT;
            }
            case UNKNOWN -> {
                return StatusCodes.INTERNAL_SERVER_ERROR;
            }
            default -> {
                return StatusCodes.IM_A_TEAPOT;
            }
        }
    }

    Map<String, String> queryToMap(String query) {
        String[] params = query.split("&");
        Map<String, String> paramsAsMap = new HashMap<>();
        for (String param : params) {
            String key = param.split("=")[0].toLowerCase();
            String value = param.split("=")[1];
            paramsAsMap.put(key, value);
        }
        return paramsAsMap;
    }

    <T> String serialize(T body) {
        Gson gson = new Gson();
        return gson.toJson(body);
    }

    /**
     * Метод возвращает String вне зависимости от исходного типа данных.
     * Валидация переданных данных осуществляется на уровне Gateway в рамках формирования ответа на запрос.
     */
    String parseFromJson(String json, String itemToParse) {
        try {
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonObject() && element.getAsJsonObject().has(itemToParse)) {
                return element.getAsJsonObject().get(itemToParse).getAsString();
            }
        } catch (JsonSyntaxException e) {
            return "";
        }

        return "";
    }

}