package com.nir.weather_svc.service;

import com.nir.weather_svc.client.WeatherApiClient;
import com.nir.weather_svc.dto.WeatherApiResponse;
import com.nir.weather_svc.dto.WeatherResponse;
import com.nir.weather_svc.exception.WeatherServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.util.List;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@TestPropertySource(properties = {
        "weather.api.url=http://test-weather-api.com/weather",
        "weather.api.key=test-api-key",
        "weather.api.units=metric",

        "spring.cache.type=none",

        "resilience4j.retry.instances.weatherRetry.max-attempts=3",
        "resilience4j.retry.instances.weatherRetry.wait-duration=10ms",

        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.minimum-number-of-calls=5",
        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.sliding-window-size=5",
        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.wait-duration-in-open-state=10s"
})
public class WeatherServiceResilienceTest {

    @Autowired
    private WeatherService weatherService;


    @MockitoBean
    private WeatherApiClient weatherApiClient;

    @Test
    void shouldRetryWhenWeatherApiFails() {

        when(weatherApiClient.getWeather("Pune"))
                .thenThrow(
                        new WeatherServiceException(
                                "Weather API unavailable"
                        )
                );

        assertThrows(
                WeatherServiceException.class,
                () -> weatherService.getWeather("Pune")
        );

        verify(
                weatherApiClient,
                times(3)
        ).getWeather("Pune");
    }

    @Test
    void shouldReturnWeatherWhenRetryEventuallySucceeds() {

        // Create Main data
        WeatherApiResponse.Main main =
                new WeatherApiResponse.Main();

        main.setTemp(30.0);
        main.setFeelsLike(31.0);
        main.setHumidity(60);

        // Create Weather data
        WeatherApiResponse.Weather weather =
                new WeatherApiResponse.Weather();

        weather.setDescription("clear sky");

        // Create Wind data
        WeatherApiResponse.Wind wind =
                new WeatherApiResponse.Wind();

        wind.setSpeed(5.0);

        // Create complete API response
        WeatherApiResponse apiResponse =
                new WeatherApiResponse();

        apiResponse.setName("Pune");
        apiResponse.setMain(main);
        apiResponse.setWeather(List.of(weather));
        apiResponse.setWind(wind);

        // First two calls fail, third succeeds
        when(weatherApiClient.getWeather("Pune"))
                .thenThrow(
                        new WeatherServiceException(
                                "Temporary failure"
                        )
                )
                .thenThrow(
                        new WeatherServiceException(
                                "Temporary failure"
                        )
                )
                .thenReturn(apiResponse);

        // Execute
        WeatherResponse result =
                weatherService.getWeather("Pune");

        // Verify result
        assertNotNull(result);

        assertEquals(
                "Pune",
                result.getCity()
        );

        assertEquals(
                30.0,
                result.getTemperature()
        );

        assertEquals(
                31.0,
                result.getFeelsLike()
        );

        assertEquals(
                60,
                result.getHumidity()
        );

        assertEquals(
                "clear sky",
                result.getDescription()
        );

        assertEquals(
                5.0,
                result.getWindSpeed()
        );

        // Verify Retry happened
        verify(
                weatherApiClient,
                times(3)
        ).getWeather("Pune");
    }


}
