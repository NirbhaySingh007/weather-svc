package com.nir.weather_svc.service;

import com.nir.weather_svc.client.WeatherApiClient;
import com.nir.weather_svc.dto.WeatherApiResponse;
import com.nir.weather_svc.dto.WeatherResponse;
import com.nir.weather_svc.exception.CityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("weather.requests"))
                .thenReturn(counter);
    }
    @Test
    void shouldReturnWeatherForCity() {

        // Arrange

        WeatherApiResponse.Main main =
                new WeatherApiResponse.Main();

        main.setTemp(28.5);
        main.setFeelsLike(29.1);
        main.setHumidity(70);


        WeatherApiResponse.Weather weather =
                new WeatherApiResponse.Weather();

        weather.setDescription("broken clouds");


        WeatherApiResponse.Wind wind =
                new WeatherApiResponse.Wind();

        wind.setSpeed(3.6);


        WeatherApiResponse apiResponse =
                new WeatherApiResponse();

        apiResponse.setName("Pune");
        apiResponse.setMain(main);
        apiResponse.setWeather(List.of(weather));
        apiResponse.setWind(wind);


        when(weatherApiClient.getWeather("Pune"))
                .thenReturn(apiResponse);


        // Act

        WeatherResponse response =
                weatherService.getWeather("Pune");


        // Assert

        assertNotNull(response);

        assertEquals(
                "Pune",
                response.getCity()
        );

        assertEquals(
                28.5,
                response.getTemperature()
        );

        assertEquals(
                29.1,
                response.getFeelsLike()
        );

        assertEquals(
                70,
                response.getHumidity()
        );

        assertEquals(
                "broken clouds",
                response.getDescription()
        );

        assertEquals(
                3.6,
                response.getWindSpeed()
        );


        // Verify

        verify(weatherApiClient)
                .getWeather("Pune");
    }

    @Test
    void shouldPropagateCityNotFoundException() {

        when(weatherApiClient.getWeather("XYZABC"))
                .thenThrow(
                        new CityNotFoundException(
                                "XYZABC"
                        )
                );

        CityNotFoundException exception =
                assertThrows(
                        CityNotFoundException.class,
                        () -> weatherService.getWeather("XYZABC")
                );

        assertEquals(
                "Weather information not found for the city: XYZABC",
                exception.getMessage()
        );

        verify(weatherApiClient)
                .getWeather("XYZABC");
    }

    @Test
    void shouldReturnNAWhenWeatherDescriptionIsMissing() {

        WeatherApiResponse.Main main =
                new WeatherApiResponse.Main();

        main.setTemp(30.0);
        main.setFeelsLike(31.0);
        main.setHumidity(60);


        WeatherApiResponse.Wind wind =
                new WeatherApiResponse.Wind();

        wind.setSpeed(4.0);


        WeatherApiResponse apiResponse =
                new WeatherApiResponse();

        apiResponse.setName("Pune");
        apiResponse.setMain(main);
        apiResponse.setWeather(List.of());
        apiResponse.setWind(wind);


        when(weatherApiClient.getWeather("Pune"))
                .thenReturn(apiResponse);


        WeatherResponse response =
                weatherService.getWeather("Pune");


        assertEquals(
                "N/A",
                response.getDescription()
        );
    }

}