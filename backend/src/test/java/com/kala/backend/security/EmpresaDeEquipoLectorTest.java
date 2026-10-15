package com.kala.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

// La tabla equipo es de otro módulo: se simula con una tabla TEMPORAL que se descarta con la transacción
// del test, de modo que no deja nada en la rama de Neon ni choca con la migración real.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(statements = {
        "CREATE TEMPORARY TABLE equipo (id BIGINT, empresa_id BIGINT)",
        "INSERT INTO equipo (id, empresa_id) VALUES (9001, 9)"
})
class EmpresaDeEquipoLectorTest {

    @Autowired
    private JdbcClient jdbc;

    @Test
    void equipoExistente_devuelveSuEmpresa() {
        assertThat(new EmpresaDeEquipoLector(jdbc).empresaDelEquipo(9001L)).contains(9L);
    }

    @Test
    void equipoInexistente_devuelveVacio() {
        assertThat(new EmpresaDeEquipoLector(jdbc).empresaDelEquipo(424242L)).isEmpty();
    }
}
