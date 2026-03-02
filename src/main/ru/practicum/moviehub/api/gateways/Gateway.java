package ru.practicum.moviehub.api.gateways;

import ru.practicum.moviehub.api.response.ApiResponse;

import java.util.Map;

public interface Gateway {

    public ApiResponse<?> getAll();

    public ApiResponse<?> getById(int id);

    public ApiResponse<?> post(Map<String, String> params);

    public ApiResponse<?> deleteAll();

    public ApiResponse<?> deleteById(int id);

}
