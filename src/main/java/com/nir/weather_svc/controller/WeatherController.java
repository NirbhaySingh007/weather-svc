package com.nir.weather_svc.controller;

import com.nir.weather_svc.dto.WeatherResponse;
import com.nir.weather_svc.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(
            summary = "Get current weather",
            description = "Returns current weather information for a city"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Weather retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid city parameter"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "City not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Weather API authentication failed"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Weather API rate limit exceeded"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Weather service unavailable"
            )
    })
    @GetMapping
    public WeatherResponse getWeather(

            @Parameter(
                    description = "Name of the city",
                    example = "Pune"
            )
            @RequestParam
            @NotBlank(message = "City cannot be empty")
            @Size(
                    min = 2,
                    max = 50,
                    message = "City must be between 2 and 50 characters"
            )
            String city) {

        return weatherService.getWeather(city.trim());
    }
}