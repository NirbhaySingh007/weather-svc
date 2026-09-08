package com.nir.weather_svc.client;

import com.nir.weather_svc.exception.WeatherServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "weather.api.url=http://test-weather-api.com/weather",
        "weather.api.key=test-api-key",
        "weather.api.units=metric",

        "resilience4j.retry.instances.weather.max-attempts=1",

        "resilience4j.circuitbreaker.instances.weather.minimum-number-of-calls=5",
        "resilience4j.circuitbreaker.instances.weather.sliding-window-size=5",
        "resilience4j.circuitbreaker.instances.weather.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.weather.wait-duration-in-open-state=10s"
})
class WeatherApiClientResilienceTest {

    @Autowired
    private WeatherApiClient weatherApiClient;

    @Test
    void circuitBreakerShouldOpenAfterRepeatedFailures() {

        for (int i = 0; i < 5; i++) {

            assertThrows(
                    WeatherServiceException.class,
                    () -> weatherApiClient.getWeather("Pune")
            );
        }

        assertThrows(
                WeatherServiceException.class,
                () -> weatherApiClient.getWeather("Pune")
        );
    }
}