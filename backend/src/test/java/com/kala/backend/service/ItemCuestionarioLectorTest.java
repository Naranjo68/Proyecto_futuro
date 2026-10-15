package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.service.ItemCuestionarioLector.ItemCuestionarioInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

// item_cuestionario es de otro módulo: se simula con una tabla TEMPORAL que se descarta con la transacción
// del test, de modo que no deja nada en la rama de Neon ni choca con la migración real.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(statements = {
        "CREATE TEMPORARY TABLE item_cuestionario (id BIGINT, cuestionario_id BIGINT, configuracion_ldp_id BIGINT)",
        "INSERT INTO item_cuestionario (id, cuestionario_id, configuracion_ldp_id) VALUES (9001, 3, 2)"
})
class ItemCuestionarioLectorTest {

    @Autowired
    private JdbcClient jdbc;

    @Test
    void itemExistente_devuelveSuCuestionarioYSuConfiguracionVigente() {
        assertThat(new ItemCuestionarioLector(jdbc).buscar(9001L))
                .contains(new ItemCuestionarioInfo(3L, 2L));
    }

    @Test
    void itemInexistente_devuelveVacio() {
        assertThat(new ItemCuestionarioLector(jdbc).buscar(424242L)).isEmpty();
    }
}
