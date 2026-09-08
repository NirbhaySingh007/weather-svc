package com.nir.weather_svc.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class AppInfoConfig implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {

        builder.withDetail(
                "app",
                java.util.Map.of(
                        "name", "Weather Service",
                        "description", "Weather API built with Spring Boot",
                        "version", "1.0.0"
                )
        );
    }
}