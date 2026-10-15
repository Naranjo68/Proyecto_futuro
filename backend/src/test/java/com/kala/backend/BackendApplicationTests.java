package com.kala.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

// Arranca la aplicacion completa contra la rama de pruebas de Neon: valida el esquema con Hibernate
// (ddl-auto=validate), aplica Flyway y cablea todos los modulos. Sin KALA_TEST_DB_URL se omite.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "kala.sesion.secreto=secreto-de-prueba",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class BackendApplicationTests {

    @Autowired
    private ApplicationContext contexto;

    @Autowired
    private Flyway flyway;

    @Test
    void contextLoads() {
        assertThat(contexto.getBeanDefinitionCount()).isPositive();
    }

    @Test
    void todasLasMigracionesEstanAplicadas() {
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("8");
    }
}
