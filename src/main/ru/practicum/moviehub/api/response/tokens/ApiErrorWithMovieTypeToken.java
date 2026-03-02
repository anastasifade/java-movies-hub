package ru.practicum.moviehub.api.response.tokens;

import com.google.gson.reflect.TypeToken;
import ru.practicum.moviehub.api.response.ApiError;
import ru.practicum.moviehub.model.Movie;

public class ApiErrorWithMovieTypeToken extends TypeToken<ApiError<Movie>> {

}
