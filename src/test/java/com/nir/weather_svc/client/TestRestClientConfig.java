package com.nir.weather_svc.client;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestRestClientConfig {

    @Bean
    RestClient.Builder testRestClientBuilder() {
        return RestClient.builder();
    }
}