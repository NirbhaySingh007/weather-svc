package com.nir.weather_svc.controller;

import com.nir.weather_svc.dto.WeatherResponse;
import com.nir.weather_svc.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {

            return new ConcurrentMapCacheManager("weather");
        }
    }

    @Test
    void shouldReturnWeather() throws Exception {

        WeatherResponse response =
                new WeatherResponse(
                        "Pune",
                        28.5,
                        29.1,
                        70,
                        "broken clouds",
                        3.6
                );

        when(weatherService.getWeather("Pune"))
                .thenReturn(response);


        mockMvc.perform(
                        get("/api/weather")
                                .param("city", "Pune")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city")
                        .value("Pune"))
                .andExpect(jsonPath("$.temperature")
                        .value(28.5))
                .andExpect(jsonPath("$.humidity")
                        .value(70))
                .andExpect(jsonPath("$.description")
                        .value("broken clouds"));
    }

    @Test
    void shouldRejectMissingCity() throws Exception {

        mockMvc.perform(
                        get("/api/weather")
                )
                .andExpect(status().isBadRequest());
    }

}