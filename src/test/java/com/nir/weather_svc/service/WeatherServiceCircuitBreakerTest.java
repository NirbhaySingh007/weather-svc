package com.nir.weather_svc.service;

import com.nir.weather_svc.client.WeatherApiClient;
import com.nir.weather_svc.exception.WeatherServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
@TestPropertySource(properties = {
        "weather.api.url=http://test-weather-api.com/weather",
        "weather.api.key=test-api-key",
        "weather.api.units=metric",

        "spring.cache.type=none",

        "resilience4j.retry.instances.weatherRetry.max-attempts=1",
        "resilience4j.retry.instances.weatherRetry.wait-duration=10ms",

        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.minimum-number-of-calls=5",
        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.sliding-window-size=5",
        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.weatherCircuitBreaker.wait-duration-in-open-state=10s"
})
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
)
public class WeatherServiceCircuitBreakerTest {


    @Autowired
    private WeatherService weatherService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @MockitoBean
    private WeatherApiClient weatherApiClient;

    @Test
    void circuitBreakerShouldOpenAfterRepeatedFailures() {

        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker(
                        "weatherCircuitBreaker"
                );

        circuitBreaker.reset();

        when(weatherApiClient.getWeather("Pune"))
                .thenThrow(
                        new WeatherServiceException(
                                "Weather API unavailable"
                        )
                );

        for (int i = 0; i < 5; i++) {

            assertThrows(
                    Exception.class,
                    () -> weatherService.getWeather("Pune")
            );
        }

        assertEquals(
                CircuitBreaker.State.OPEN,
                circuitBreaker.getState()
        );

        verify(
                weatherApiClient,
                times(5)
        ).getWeather("Pune");
    }

    @Test
    void openCircuitBreakerShouldRejectFurtherCalls() {

        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker(
                        "weatherCircuitBreaker"
                );

        circuitBreaker.reset();

        when(weatherApiClient.getWeather("Pune"))
                .thenThrow(
                        new WeatherServiceException(
                                "Weather API unavailable"
                        )
                );

        // Generate five failures
        for (int i = 0; i < 5; i++) {

            assertThrows(
                    Exception.class,
                    () -> weatherService.getWeather("Pune")
            );
        }

        // Verify that the circuit is OPEN
        assertEquals(
                CircuitBreaker.State.OPEN,
                circuitBreaker.getState()
        );

        // Sixth call should be rejected by Circuit Breaker
        assertThrows(
                CallNotPermittedException.class,
                () -> weatherService.getWeather("Pune")
        );

        // Sixth call must NOT reach the external API client
        verify(
                weatherApiClient,
                times(5)
        ).getWeather("Pune");
    }
}
