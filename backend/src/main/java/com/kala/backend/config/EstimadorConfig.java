package com.kala.backend.config;

import com.kala.backend.service.EstimadorDesesgado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EstimadorConfig {
    
     @Bean
    public EstimadorDesesgado estimadorDesesgado(
            @Value("${kala.ldp.probabilidad-verdad}") double p) {
        return new EstimadorDesesgado(p);
    }
}
