package com.kala.backend.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Reloj en la zona de la aplicación (America/Lima), inyectable para que el tiempo sea controlable en las pruebas.
@Configuration(proxyBeanMethods = false)
public class RelojConfig {

    @Bean
    public Clock reloj(@Value("${kala.ventana.zona}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
