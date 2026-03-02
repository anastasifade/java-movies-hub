package ru.practicum.moviehub.api.response;

public class ApiError<T> {
    private final String error;
    private final T details;

    public ApiError(String error, T details) {
        this.error = error;
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public T getDetails() {
        return details;
    }
}
