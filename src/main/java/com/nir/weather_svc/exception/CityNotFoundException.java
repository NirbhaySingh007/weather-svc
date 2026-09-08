package com.nir.weather_svc.exception;

public class CityNotFoundException extends RuntimeException{

    public CityNotFoundException(String city){
        super("Weather information not found for the city: "+city);
    }
}
