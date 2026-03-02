package ru.practicum.moviehub.api.response;

import ru.practicum.moviehub.api.enums.FailureState;

import java.util.Optional;

public class ApiResponse<T> {

    private boolean isSuccess;
    private T body;
    private FailureState failureState;

    public ApiResponse() {}

    public ApiResponse(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public ApiResponse(boolean isSuccess, T body) {
        this.isSuccess = isSuccess;
        this.body = body;
    }

    public ApiResponse(boolean isSuccess, FailureState failureState) {
        this.isSuccess = isSuccess;
        this.failureState = failureState;
    }

    public ApiResponse(boolean isSuccess, FailureState failureState, T body) {
        this.isSuccess = isSuccess;
        this.failureState = failureState;
        this.body = body;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public Optional<T> getBody() {
        return Optional.ofNullable(body);
    }

    public Optional<FailureState> getFailureState() {
        return Optional.ofNullable(failureState);
    }

    public void setBody(T body) {
        this.body = body;
    }

    public void setFailureState(FailureState failureState) {
        this.failureState = failureState;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
