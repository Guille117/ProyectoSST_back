package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class RelojConfig {

    /** Zona horaria del sanatorio: la misma de los equipos que usan el sistema. */
    @Value("${app.timezone:America/Guatemala}")
    private String zona;

    /**
     * Fija la zona del JVM al arrancar, para que todos los {@code LocalDateTime.now()} del
     * backend usen la hora local del sanatorio aunque el servidor o el contenedor estén en UTC.
     */
    @PostConstruct
    public void configurarZonaHoraria() {
        TimeZone.setDefault(TimeZone.getTimeZone(zona));
    }

    @Bean
    public Clock relojSistema() {
        return Clock.system(ZoneId.of(zona));
    }
}
