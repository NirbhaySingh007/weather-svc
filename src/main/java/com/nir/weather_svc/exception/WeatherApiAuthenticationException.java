package com.nir.weather_svc.exception;

public class WeatherApiAuthenticationException extends RuntimeException {

    public WeatherApiAuthenticationException(String msg) {
        super(msg);
    }
}
