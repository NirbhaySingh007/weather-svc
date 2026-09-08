package com.nir.weather_svc.exception;

public class WeatherApiRateLimitException extends RuntimeException{

    public WeatherApiRateLimitException(String msg){
        super(msg);
    }
}
