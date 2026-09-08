package com.nir.weather_svc;

import com.nir.weather_svc.config.WeatherProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(WeatherProperties.class)
public class WeatherSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherSvcApplication.class, args);
	}

}
