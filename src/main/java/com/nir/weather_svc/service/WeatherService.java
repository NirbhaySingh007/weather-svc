package com.nir.weather_svc.service;

import com.nir.weather_svc.client.WeatherApiClient;
import com.nir.weather_svc.dto.WeatherApiResponse;
import com.nir.weather_svc.dto.WeatherResponse;
import com.nir.weather_svc.exception.WeatherServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final MeterRegistry meterRegistry;

    public WeatherService(WeatherApiClient weatherApiClient, MeterRegistry meterRegistry) {
        this.weatherApiClient = weatherApiClient;
        this.meterRegistry = meterRegistry;
    }


    @Retry(name = "weatherRetry")
    @CircuitBreaker(
            name = "weatherCircuitBreaker",
            fallbackMethod = "weatherFallback"
    )
    @Cacheable(
            value = "weather",
            key = "#city.toLowerCase()"
    )
    public WeatherResponse getWeather(String city) {

        meterRegistry.counter("weather.requests").increment();

        WeatherApiResponse apiResponse =
                weatherApiClient.getWeather(city);

        String description = apiResponse.getWeather()
                .isEmpty()
                ? "N/A"
                : apiResponse.getWeather()
                .get(0)
                .getDescription();

        return new WeatherResponse(
                apiResponse.getName(),
                apiResponse.getMain().getTemp(),
                apiResponse.getMain().getFeelsLike(),
                apiResponse.getMain().getHumidity(),
                description,
                apiResponse.getWind().getSpeed()
        );
    }

    public WeatherApiResponse weatherFallback(
            String city,
            Throwable throwable) {

        throw new WeatherServiceException(
                "Weather service is currently unavailable. Please try again later.",
                throwable
        );
    }
}