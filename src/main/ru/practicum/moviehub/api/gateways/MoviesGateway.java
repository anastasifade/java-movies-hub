package ru.practicum.moviehub.api.gateways;

import ru.practicum.moviehub.api.response.ApiError;
import ru.practicum.moviehub.api.response.ApiResponse;
import ru.practicum.moviehub.api.enums.FailureState;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MoviesGateway implements Gateway {

    private final MoviesStore store;

    public MoviesGateway(final MoviesStore store) {
        this.store = store;
    }

    @Override
    public ApiResponse<List<Movie>> getAll() {
        return new ApiResponse<>(true, store.getMovies());
    }

    public ApiResponse<?> getByYear(int year) {
        if (year < store.getMinYear() || year > store.getMaxYear()) {
            return new ApiResponse<>(false, FailureState.INVALID_REQUEST,
                    String.format("Поиск по году выхода возможен в интервале [%d-%d].",
                            store.getMinYear(), store.getMaxYear()));
        }
        return new ApiResponse<>(true, store.getMoviesByYear(year));
    }

    @Override
    public ApiResponse<?> getById(int id) {
        if (id <= 0) {
            return new ApiResponse<>(false, FailureState.INVALID_REQUEST,
                    "Универсальный идентификатор для поиска должен быть больше 0.");
        }

        Optional<Movie> movie = store.getMovieById(id);
        if (movie.isEmpty()) {
            return new ApiResponse<>(false, FailureState.ITEM_NOT_FOUND, "Фильм не найден.");
        }

        return new ApiResponse<>(true, movie.get());
    }

    @Override
    public ApiResponse<?> post(Map<String, String> params) {
        String title = params.get("title");
        String year = params.get("year");
        List<String> validationErrors = validatePostParams(title, year);

        if (!validationErrors.isEmpty()) {
            return new ApiResponse<>(false, FailureState.INVALID_BODY,
                    new ApiError<>("Ошибка валидации", validationErrors));
        }

        int yearInt = Integer.parseInt(year);
        Movie movie = new Movie(title, yearInt);
        if (store.has(movie)) {
            return new ApiResponse<>(false, FailureState.ITEM_ALREADY_EXISTS,
                    new ApiError<>("Фильм уже существует.", store.getMovie(title, yearInt).get()));
        }

        return new ApiResponse<>(true, store.addMovie(movie));
    }

    private List<String> validatePostParams(String title, String year) {
        List<String> errors = new ArrayList<>();

        if (title == null || title.isEmpty()) {
            errors.add("Поле [title] не может быть пустым.");
        } else if (title.length() > 100) {
            errors.add("Поле [title] не должно быть длинее 100 символов.");
        }

        if (year == null || year.isEmpty()) {
            errors.add("Поле [year] не может быть пустым.");
        } else {
            try {
                int yearAsInt = Integer.parseInt(year);
                if (yearAsInt < store.getMinYear() || yearAsInt > store.getMaxYear()) {
                    errors.add(String.format("Поле [year] выходит за границы поиска. " +
                            "Ожидается численное значение в диапазоне [%d-%d].", store.getMinYear(), store.getMaxYear()));
                }
            } catch (NumberFormatException e) {
                errors.add(String.format("Поле [year] содержит нелегальные символы. " +
                        "Ожидается численное значение в диапазоне [%d-%d].", store.getMinYear(), store.getMaxYear()));
            }
        }

        return errors;
    }

    @Override
    public ApiResponse<?> deleteAll() {
        store.deleteAll();
        return new ApiResponse<>(true);
    }

    @Override
    public ApiResponse<?> deleteById(int id) {
        if (id <= 0) {
            return new ApiResponse<>(false, FailureState.INVALID_REQUEST,
                    "Универсальный идентификатор для поиска должен быть больше 0.");
        }

        return store.delete(id) ? new ApiResponse<>(true) :
                new ApiResponse<>(false, FailureState.ITEM_NOT_FOUND, "Фильм не найден.");
    }

}



