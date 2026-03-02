package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.api.gateways.MoviesGateway;
import ru.practicum.moviehub.http.handlers.MoviesHandler;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.ListOfMoviesTypeToken;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoviesApiTest {

    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final String URL_MOVIES = "http://localhost:8080/movies";
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final int PORT = 8080;

    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store;
    private static Gson gson;

    private static Movie firstMovie;
    private static Movie godfather;
    private static Movie upcomingMovie;

    @BeforeAll
    static void beforeAll() throws IOException {
        store = new MoviesStore();

        server = new MoviesServer(PORT);
        server.createContext("/movies", new MoviesHandler(new MoviesGateway(store)));
        server.start();
        gson = new Gson();

        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        godfather = new Movie("The Godfather", 1972);
        firstMovie = new Movie("Roundhay Garden Scene", 1888);
        upcomingMovie = new Movie("{placeholder title}", LocalDate.now().getYear() + 1);
    }

    @BeforeEach
    void beforeEach() {
        store.deleteAll();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void unsupportedMethod_returns_405() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        // Проверка на пустом хранилище
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(405, response.statusCode());
    }


    // GET /movies

    @Test
    void getMovies_headersShouldContainDataFormatAndCharset() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String responseContentType = response.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals(CONTENT_TYPE, responseContentType);
    }

    @Test
    void getMovies_returns_200() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .GET()
                .build();

        // Проверка на пустом хранилище
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(200, response.statusCode());

        // Проверка после добавления фильмов
        store.addMovie(godfather);
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals("[]", response.body());
    }

    @Test
    void getMovies_returnsListOfAddedMovies() throws IOException, InterruptedException {
        List<Movie> addedMovies = new ArrayList<>();

        // проверка при добавлении одного фильма
        store.addMovie(godfather);
        addedMovies.add(godfather);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String respJson = new String(response.body().getBytes(), DEFAULT_CHARSET);
        List<Movie> respMovies = gson.fromJson(respJson, new ListOfMoviesTypeToken().getType());

        Assertions.assertEquals(addedMovies, respMovies);

        // проверка при добавлении нескольких фильмов
        store.addMovie(firstMovie);
        store.addMovie(upcomingMovie);
        addedMovies.add(firstMovie);
        addedMovies.add(upcomingMovie);

        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        respJson = new String(response.body().getBytes(), DEFAULT_CHARSET);
        respMovies = gson.fromJson(respJson, new ListOfMoviesTypeToken().getType());

        Assertions.assertEquals(addedMovies, respMovies);
    }


    // GET /movies?year=YYYY

    @Test
    void getMoviesByYear_onSuccess_headersContainFormatAndCharset() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "?year=" + godfather.getYear()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String responseContentType = response.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals(CONTENT_TYPE, responseContentType);
    }

    @Test
    void getMoviesByYear_onSuccessReturns_200() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "?year=" + godfather.getYear()))
                .GET()
                .build();

        // Проверка на пустом хранилище
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(200, response.statusCode());

        // Проверка после добавления фильмов
        store.addMovie(godfather);
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    void getMoviesByYear_onInvalidQuery_returns_400() throws IOException, InterruptedException {
        // Проверка запроса с нечисленным значением year
        HttpRequest requestMalformedUri = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "?year=YYYY"))
                .GET()
                .build();

        // Проверка запроса, где year выходит за установленные границы

        HttpRequest requestInvalidYear = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "?year=33"))
                .GET()
                .build();

        // Проверка на пустом хранилище
        HttpResponse<String> response = client.send(requestMalformedUri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(400, response.statusCode());

        response = client.send(requestInvalidYear, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(400, response.statusCode());

        // Проверка после добавления фильмов
        store.addMovie(godfather);

        response = client.send(requestMalformedUri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(400, response.statusCode());

        response = client.send(requestInvalidYear, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void getMoviesByYear_whenEmpty_returnsEmptyArray() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "?year=2026"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[]", response.body());
    }

    @Test
    void getMoviesByYear_returnsMoviesReleasedInYear() throws IOException, InterruptedException {
        List<Movie> moviesByYear = new ArrayList<>();

        store.addMovie(godfather);
        store.addMovie(firstMovie);
        store.addMovie(upcomingMovie);

        moviesByYear.add(godfather);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "?year=" + godfather.getYear()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String respJson = new String(response.body().getBytes(), DEFAULT_CHARSET);
        List<Movie> respMovies = gson.fromJson(respJson, new ListOfMoviesTypeToken().getType());

        Assertions.assertEquals(moviesByYear, respMovies);

    }


    // GET /movies/{id}

    @Test
    void getMoviesById_onSuccess_headersContainFormatAndCharset() throws IOException, InterruptedException {
        store.addMovie(godfather);
        Movie godfatherWithId = store.getMovieById(1).get();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/" + godfatherWithId.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String responseContentType = response.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals(CONTENT_TYPE, responseContentType);
    }

    @Test
    void getMoviesById_onSuccessReturns_200() throws IOException, InterruptedException {
        store.addMovie(godfather);
        Movie godfatherWithId = store.getMovieById(1).get();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/" + godfatherWithId.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    void getMovieById_whenIllegalId_returns_400() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/abcd"))
                .GET()
                .build();

        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void getMoviesById_whenMovieNotFound_returns_404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/99999999"))
                .GET()
                .build();

        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void getMoviesById_returnsMovieWithCorrectId() throws IOException, InterruptedException {
        store.addMovie(godfather);
        Movie godfatherWithId = store.getMovieById(1).get();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/" + godfatherWithId.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String json = new String(response.body().getBytes(), DEFAULT_CHARSET);
        Movie respMovie = gson.fromJson(json, Movie.class);

        Assertions.assertEquals(godfatherWithId.getId(), respMovie.getId());
        Assertions.assertEquals(godfatherWithId, respMovie);
    }


    // POST /movies

    @Test
    void postMovie_onSuccess_headersContainFormatAndCharset() throws IOException, InterruptedException {
        String movieJson = gson.toJson(godfather);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(movieJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String responseContentType = response.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals(CONTENT_TYPE, responseContentType);
    }

    @Test
    void postMovie_onSuccessReturns_201() throws IOException, InterruptedException {
        String movieJson = gson.toJson(godfather);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(movieJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(201, response.statusCode());
    }

    @Test
    void postMovie_ifMovieAlreadyExists_returns_409() throws IOException, InterruptedException {
        String movieJson = gson.toJson(godfather);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(movieJson, DEFAULT_CHARSET))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString(DEFAULT_CHARSET));

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(409, response.statusCode());
    }

    @Test
    void postMovie_forInvalidRequestBody_returns_422() throws IOException, InterruptedException {
        String emptyTitle = "{\"title\": \"\",\"year\": \"2021\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(emptyTitle, DEFAULT_CHARSET))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(422, response.statusCode());

        String titleTooLong = "{\"title\": \"" + "a".repeat(101) + "\",\"year\": \"2021\"}";
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(titleTooLong, DEFAULT_CHARSET))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(422, response.statusCode());

        String emptyYear = "{\"title\": \"title\",\"year\": \"\"}";
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(emptyYear, DEFAULT_CHARSET))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(422, response.statusCode());

        String illegalYear = "{\"title\": \"title\",\"year\": \"YYYY\"}";
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(illegalYear, DEFAULT_CHARSET))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(422, response.statusCode());

        String yearOutOfBounds = "{\"title\": \"title\",\"year\": \"33\"}";
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(yearOutOfBounds, DEFAULT_CHARSET))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(422, response.statusCode());

        String malformedJson = "{this isn't even a real json object}";
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(malformedJson, DEFAULT_CHARSET))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(422, response.statusCode());
    }

    @Test
    void postMovie_forInvalidHeader_returns_415() throws IOException, InterruptedException {
        String movieJson = gson.toJson(godfather);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", "text/html; charset=UTF-16")
                .POST(HttpRequest.BodyPublishers.ofString(movieJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(415, response.statusCode());
    }

    @Test
    void postMovie_onSuccess_returnsMovieWithId() throws IOException, InterruptedException {
        String movieJson = gson.toJson(godfather);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .header("Content-Type", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(movieJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String respJson = new String(response.body().getBytes(), DEFAULT_CHARSET);
        Movie respMovie = gson.fromJson(respJson, Movie.class);

        Assertions.assertTrue(respMovie.getId() > 0);
    }


    // DELETE /movies

    @Test
    void deleteMovies_returns_204() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .DELETE()
                .build();

        // проверка на пустом списке
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(204, response.statusCode());

        // проверка после добавления фильмов
        store.addMovie(godfather);
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    void deleteMovies_deletesAllMovies() throws IOException, InterruptedException {
        store.addMovie(godfather);
        store.addMovie(firstMovie);
        store.addMovie(upcomingMovie);

        Assertions.assertFalse(store.getMovies().isEmpty());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES))
                .DELETE()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString(DEFAULT_CHARSET));

        Assertions.assertTrue(store.getMovies().isEmpty());
    }

    // DELETE /movies/{id}

    @Test
    void deleteMovieById_onSuccessReturns_204() throws IOException, InterruptedException {
        store.addMovie(godfather);
        int id = godfather.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    void deleteMovieById_forMalformedId_returns_400() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/abc"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void deleteMovieById_ifMovieNotFound_returns_404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/999999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void deleteMovieById_onSuccess_deletesMovieFromStore() throws IOException, InterruptedException {
        store.addMovie(firstMovie);
        store.addMovie(godfather);
        store.addMovie(upcomingMovie);

        Assertions.assertTrue(store.getMovies().contains(godfather));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_MOVIES + "/" + godfather.getId()))
                .DELETE()
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString(DEFAULT_CHARSET));

        Assertions.assertFalse(store.getMovies().contains(godfather));
    }

}