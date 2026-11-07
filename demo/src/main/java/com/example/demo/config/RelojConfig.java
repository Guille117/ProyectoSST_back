package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelojConfig {

    @Bean
    public Clock relojSistema(@Value("${app.timezone:America/Guatemala}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
