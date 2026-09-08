package com.nir.weather_svc.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(
                        Timeout.ofSeconds(3)
                )
                .setResponseTimeout(
                        Timeout.ofSeconds(5)
                )
                .build();

        CloseableHttpClient httpClient =
                HttpClients.custom()
                        .setDefaultRequestConfig(requestConfig)
                        .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(
                        httpClient
                );

        return RestClient.builder()
                .requestFactory(requestFactory);
    }
}