package com.nir.weather_svc.client;

import com.nir.weather_svc.exception.CityNotFoundException;
import com.nir.weather_svc.exception.WeatherApiAuthenticationException;
import com.nir.weather_svc.exception.WeatherApiRateLimitException;
import com.nir.weather_svc.exception.WeatherServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.client.RestClient;
import com.nir.weather_svc.config.WeatherProperties;
import com.nir.weather_svc.dto.WeatherApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import static org.junit.jupiter.api.Assertions.*;

public class WeatherApiClientTest {


    private WeatherApiClient weatherApiClient;

    private MockRestServiceServer mockServer;
    private MeterRegistry meterRegistry;


    @BeforeEach
    void setUp() {

        RestClient.Builder builder =
                RestClient.builder();

        mockServer =
                MockRestServiceServer.bindTo(builder)
                        .build();

        WeatherProperties properties =
                new WeatherProperties();

        properties.setUrl(
                "http://test-weather-api.com/weather"
        );

        properties.setKey("test-api-key");

        properties.setUnits("metric");

        meterRegistry = new SimpleMeterRegistry();

        weatherApiClient =
                new WeatherApiClient(
                        builder,
                        properties,
                        meterRegistry
                );
    }


    @Test
    void shouldReturnWeatherWhenApiReturns200() {

        String jsonResponse = """
            {
              "name": "Pune",
              "main": {
                "temp": 28.5,
                "feels_like": 29.1,
                "humidity": 70
              },
              "weather": [
                {
                  "description": "broken clouds"
                }
              ],
              "wind": {
                "speed": 3.6
              }
            }
            """;


        mockServer.expect(
                        requestTo(
                                "http://test-weather-api.com/weather" +
                                        "?q=Pune" +
                                        "&appid=test-api-key" +
                                        "&units=metric"
                        )
                )
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                jsonResponse,
                                org.springframework.http.MediaType.APPLICATION_JSON
                        )
                );


        WeatherApiResponse response =
                weatherApiClient.getWeather("Pune");


        assertNotNull(response);

        assertEquals(
                "Pune",
                response.getName()
        );

        assertEquals(
                28.5,
                response.getMain().getTemp()
        );

        assertEquals(
                29.1,
                response.getMain().getFeelsLike()
        );

        assertEquals(
                70,
                response.getMain().getHumidity()
        );

        assertEquals(
                "broken clouds",
                response.getWeather()
                        .get(0)
                        .getDescription()
        );

        assertEquals(
                3.6,
                response.getWind().getSpeed()
        );


        mockServer.verify();
    }


    @Test
    void shouldThrowCityNotFoundExceptionWhenApiReturns404() {

        mockServer.expect(
                        requestTo(
                                "http://test-weather-api.com/weather" +
                                        "?q=XYZABC" +
                                        "&appid=test-api-key" +
                                        "&units=metric"
                        )
                )
                .andExpect(
                        method(org.springframework.http.HttpMethod.GET)
                )
                .andRespond(
                        withStatus(
                                org.springframework.http.HttpStatus.NOT_FOUND
                        )
                );


        CityNotFoundException exception =
                assertThrows(
                        CityNotFoundException.class,
                        () -> weatherApiClient.getWeather("XYZABC")
                );


        assertEquals(
                "Weather information not found for the city: XYZABC",
                exception.getMessage()
        );


        mockServer.verify();
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenApiReturns401() {

        mockServer.expect(
                        requestTo(
                                "http://test-weather-api.com/weather" +
                                        "?q=Pune" +
                                        "&appid=test-api-key" +
                                        "&units=metric"
                        )
                )
                .andRespond(
                        withStatus(
                                org.springframework.http.HttpStatus.UNAUTHORIZED
                        )
                );


        WeatherApiAuthenticationException exception =
                assertThrows(
                        WeatherApiAuthenticationException.class,
                        () -> weatherApiClient.getWeather("Pune")
                );


        assertEquals(
                "Weather API authentication failed",
                exception.getMessage()
        );


        mockServer.verify();
    }

    @Test
    void shouldThrowRateLimitExceptionWhenApiReturns429() {

        mockServer.expect(
                        requestTo(
                                "http://test-weather-api.com/weather" +
                                        "?q=Pune" +
                                        "&appid=test-api-key" +
                                        "&units=metric"
                        )
                )
                .andRespond(
                        withStatus(
                                org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
                        )
                );


        WeatherApiRateLimitException exception =
                assertThrows(
                        WeatherApiRateLimitException.class,
                        () -> weatherApiClient.getWeather("Pune")
                );


        assertEquals(
                "Weather API rate limit exceeded",
                exception.getMessage()
        );


        mockServer.verify();
    }

    @Test
    void shouldThrowWeatherServiceExceptionWhenApiReturns500() {

        mockServer.expect(
                        requestTo(
                                "http://test-weather-api.com/weather" +
                                        "?q=Pune" +
                                        "&appid=test-api-key" +
                                        "&units=metric"
                        )
                )
                .andRespond(
                        withStatus(
                                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
                        )
                );


        WeatherServiceException exception =
                assertThrows(
                        WeatherServiceException.class,
                        () -> weatherApiClient.getWeather("Pune")
                );


        assertEquals(
                "Weather service is currently unavailable",
                exception.getMessage()
        );


        mockServer.verify();
    }



}
