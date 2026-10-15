package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.repository.DashboardRepository.FilaItem;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

// Las tablas son de otros módulos: se simulan con tablas TEMPORALES (solo las columnas que usa la consulta)
// que se descartan con la transacción del test y no dejan nada en la rama de Neon.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(statements = {
        "CREATE TEMPORARY TABLE equipo (id BIGINT, empresa_id BIGINT)",
        "CREATE TEMPORARY TABLE agregado_item (equipo_id BIGINT, item_id BIGINT, configuracion_ldp_id BIGINT, "
                + "fecha DATE, suma_reportada NUMERIC(14,4), total_respuestas INT)",
        "CREATE TEMPORARY TABLE configuracion_ldp (id BIGINT, mecanismo VARCHAR(25), "
                + "probabilidad_verdad DECIMAL(4,3), epsilon DECIMAL(6,3))",
        "CREATE TEMPORARY TABLE item_cuestionario (id BIGINT, tema VARCHAR(100), escala_id BIGINT, "
                + "configuracion_ldp_id BIGINT)",
        "CREATE TEMPORARY TABLE escala_respuesta (id BIGINT, valor_min SMALLINT, valor_max SMALLINT)",
        "INSERT INTO equipo VALUES (10, 1), (11, 2)",
        "INSERT INTO escala_respuesta VALUES (1, 0, 1)",
        "INSERT INTO configuracion_ldp VALUES (2, 'RANDOMIZED_RESPONSE', 0.750, NULL)",
        "INSERT INTO item_cuestionario VALUES (3, 'Pulso', 1, 2)",
        // Equipo 10: dos días dentro de la semana del lunes 2026-03-09 (suman 20 y 40), uno antes y uno después.
        "INSERT INTO agregado_item VALUES (10, 3, 2, DATE '2026-03-09', 8, 12), (10, 3, 2, DATE '2026-03-15', 12, 28)",
        "INSERT INTO agregado_item VALUES (10, 3, 2, DATE '2026-03-08', 99, 99), (10, 3, 2, DATE '2026-03-16', 99, 99)",
        // Otro equipo en la misma semana: no debe sumarse.
        "INSERT INTO agregado_item VALUES (11, 3, 2, DATE '2026-03-10', 50, 50)"
})
class DashboardRepositoryTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 3, 9);

    @Autowired
    private JdbcClient jdbc;

    @Test
    void sumaLosDiasDeLaVentanaDelEquipo_yNoLosDeFueraNiLosDeOtroEquipo() {
        List<FilaItem> filas = new DashboardRepository(jdbc).agregadosDeVentana(10L, LUNES, LUNES.plusDays(6));

        assertThat(filas).hasSize(1);
        FilaItem fila = filas.get(0);
        assertThat(fila.itemId()).isEqualTo(3L);
        assertThat(fila.tema()).isEqualTo("Pulso");
        assertThat(fila.mecanismo()).isEqualTo("RANDOMIZED_RESPONSE");
        assertThat(fila.probabilidadVerdad()).isEqualByComparingTo("0.750");
        assertThat(fila.valorMin()).isZero();
        assertThat(fila.valorMax()).isEqualTo(1);
        assertThat(fila.suma()).isEqualByComparingTo("20");
        assertThat(fila.n()).isEqualTo(40L);
    }

    @Test
    void unaVentanaSinRespuestas_devuelveListaVacia() {
        assertThat(new DashboardRepository(jdbc).agregadosDeVentana(10L, LUNES.plusDays(14), LUNES.plusDays(20)))
                .isEmpty();
    }

    @Test
    void equipoPerteneceAEmpresa_distingueLaEmpresaDelEquipo() {
        DashboardRepository repositorio = new DashboardRepository(jdbc);

        assertThat(repositorio.equipoPerteneceAEmpresa(10L, 1L)).isTrue();
        assertThat(repositorio.equipoPerteneceAEmpresa(10L, 2L)).isFalse();
        assertThat(repositorio.equipoPerteneceAEmpresa(999L, 1L)).isFalse();
    }
}
