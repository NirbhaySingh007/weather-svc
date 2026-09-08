package com.nir.weather_svc.dto;

public class WeatherResponse {

    private String city;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String description;
    private double windSpeed;

    public WeatherResponse(
            String city,
            double temperature,
            double feelsLike,
            int humidity,
            String description,
            double windSpeed) {

        this.city = city;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.windSpeed = windSpeed;
    }

    public String getCity() {
        return city;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getDescription() {
        return description;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}