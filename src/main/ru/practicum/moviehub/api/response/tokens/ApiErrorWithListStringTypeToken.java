package ru.practicum.moviehub.api.response.tokens;

import com.google.gson.reflect.TypeToken;
import ru.practicum.moviehub.api.response.ApiError;

import java.util.List;

public class ApiErrorWithListStringTypeToken extends TypeToken<ApiError<List<String>>> {

}
