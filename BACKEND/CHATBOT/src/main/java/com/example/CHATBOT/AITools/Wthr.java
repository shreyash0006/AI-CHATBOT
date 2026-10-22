package com.example.CHATBOT.AITools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class Wthr {

    private final RestClient restClient;
    private final String apiKey;

    public Wthr(
            RestClient.Builder restClientBuilder,
            @Value("${weather.api.base-url:https://api.openweathermap.org}") String baseUrl,
            @Value("${weather.api.key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    /**
     * Clean DTO returned to the LLM.
     * Contains only the essential metrics: temperature, humidity, conditions, etc.
     */
    public record WeatherInfo(
            String city,
            double temperatureCelsius,
            double feelsLikeCelsius,
            int humidityPercent,
            double windSpeedMps,
            String condition
    ) {}

    @Tool(description = "Get real-time weather details including temperature, humidity, and atmospheric condition for a given city")
    public WeatherInfo getCurrentWeather(
            @ToolParam(description = "City name and optional country code (e.g. 'Mysore', 'Mysore,IN', 'London,UK')", required = true)
            String city
    ) {
        OpenWeatherApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("q", city)
                        .queryParam("appid", this.apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .body(OpenWeatherApiResponse.class);

        if (response == null || response.main() == null) {
            throw new IllegalStateException("Failed to parse weather data for city: " + city);
        }

        String condition = (response.weather() != null && !response.weather().isEmpty())
                ? response.weather().get(0).description()
                : "N/A";

        return new WeatherInfo(
                response.name(),
                response.main().temp(),
                response.main().feelsLike(),
                response.main().humidity(),
                response.wind() != null ? response.wind().speed() : 0.0,
                condition
        );
    }

    // --- Internal DTOs to map OpenWeatherMap API JSON response ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenWeatherApiResponse(
            String name,
            MainMetrics main,
            WindMetrics wind,
            List<WeatherSummary> weather
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MainMetrics(
            double temp,
            @JsonProperty("feels_like") double feelsLike,
            int humidity
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WindMetrics(
            double speed
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WeatherSummary(
            String main,
            String description
    ) {}
}