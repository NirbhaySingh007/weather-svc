package com.nir.weather_svc.client;

import com.nir.weather_svc.config.WeatherProperties;
import com.nir.weather_svc.dto.WeatherApiResponse;
import com.nir.weather_svc.exception.CityNotFoundException;
import com.nir.weather_svc.exception.WeatherApiAuthenticationException;
import com.nir.weather_svc.exception.WeatherApiRateLimitException;
import com.nir.weather_svc.exception.WeatherServiceException;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WeatherApiClient {

    private static final Logger logger =
            LoggerFactory.getLogger(WeatherApiClient.class);

    private final RestClient restClient;
    private final WeatherProperties weatherProperties;
    private final MeterRegistry meterRegistry;

    public WeatherApiClient(
            RestClient.Builder builder,
            WeatherProperties weatherProperties, MeterRegistry meterRegistry) {

        this.restClient = builder.build();
        this.weatherProperties = weatherProperties;
        this.meterRegistry = meterRegistry;
    }


    public WeatherApiResponse getWeather(String city) {

        logger.info("Fetching weather for city: {}", city);
        meterRegistry.counter("weather.api.calls").increment();

        try {

            String uri = UriComponentsBuilder
                    .fromUriString(weatherProperties.getUrl())
                    .queryParam("q", city)
                    .queryParam("appid", weatherProperties.getKey())
                    .queryParam("units", weatherProperties.getUnits())
                    .toUriString();

            return restClient.get()
                    .uri(uri)
                    .retrieve()

                    // 401 - Invalid API key
                    .onStatus(
                            status -> status.value() == 401,
                            (request, response) -> {
                                throw new WeatherApiAuthenticationException(
                                        "Weather API authentication failed"
                                );
                            }
                    )

                    // 404 - City not found
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new CityNotFoundException(city);
                            }
                    )

                    // 429 - Rate limit exceeded
                    .onStatus(
                            status -> status.value() == 429,
                            (request, response) -> {
                                throw new WeatherApiRateLimitException(
                                        "Weather API rate limit exceeded"
                                );
                            }
                    )

                    // Other 4xx
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                throw new WeatherServiceException(
                                        "Weather API request failed"
                                );
                            }
                    )

                    // 5xx
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new WeatherServiceException(
                                        "Weather service is currently unavailable"
                                );
                            }
                    )

                    .body(WeatherApiResponse.class);

        } catch (CityNotFoundException exception) {

            throw exception;

        } catch (WeatherApiAuthenticationException exception) {

            throw exception;

        } catch (WeatherApiRateLimitException exception) {

            throw exception;

        } catch (WeatherServiceException exception) {

            throw exception;

        } catch (Exception exception) {

            logger.error(
                    "Error while fetching weather for city: {}",
                    city,
                    exception
            );

            throw new WeatherServiceException(
                    "Unable to communicate with weather service",
                    exception
            );
        }
    }



}