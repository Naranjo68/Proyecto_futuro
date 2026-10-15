
package com.kala.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepository {

    private static final String SQL_DASHBOARD = """
        SELECT a.item_id,
               i.tema,
               a.configuracion_ldp_id,
               c.mecanismo,
               c.probabilidad_verdad,
               c.epsilon,
               e.valor_min,
               e.valor_max,
               SUM(a.suma_reportada) AS suma,
               SUM(a.total_respuestas) AS n
        FROM agregado_item a
        JOIN configuracion_ldp c
          ON c.id = a.configuracion_ldp_id
        JOIN item_cuestionario i
          ON i.id = a.item_id
        JOIN escala_respuesta e
          ON e.id = i.escala_id
        WHERE a.equipo_id = :equipoId
          AND a.fecha BETWEEN :desde AND :hasta
        GROUP BY a.item_id, i.tema,
                 a.configuracion_ldp_id, c.mecanismo,
                 c.probabilidad_verdad, c.epsilon,
                 e.valor_min, e.valor_max
        """;

    private final JdbcClient jdbcClient;

    public DashboardRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ItemAgregado> consultar(
            Long equipoId,
            LocalDate desde,
            LocalDate hasta) {

        return jdbcClient.sql(SQL_DASHBOARD)
                .param("equipoId", equipoId)
                .param("desde", desde)
                .param("hasta", hasta)
                .query((rs, rowNum) -> new ItemAgregado(
                        rs.getLong("item_id"),
                        rs.getString("tema"),
                        rs.getLong("configuracion_ldp_id"),
                        rs.getString("mecanismo"),
                        rs.getBigDecimal("probabilidad_verdad"),
                        rs.getBigDecimal("epsilon"),
                        rs.getInt("valor_min"),
                        rs.getInt("valor_max"),
                        rs.getBigDecimal("suma"),
                        rs.getLong("n")
                ))
                .list();
    }

    public boolean equipoPerteneceAEmpresa(
            Long equipoId,
            Long empresaId) {

        Integer coincidencias = jdbcClient.sql("""
                SELECT COUNT(*)
                FROM equipo
                WHERE id = :equipoId
                  AND empresa_id = :empresaId
                """)
                .param("equipoId", equipoId)
                .param("empresaId", empresaId)
                .query(Integer.class)
                .single();

        return coincidencias != null && coincidencias > 0;
    }

    public void registrarConsulta(
            Long usuarioId,
            Long equipoId,
            LocalDate desde,
            LocalDate hasta,
            String resultado) {

        jdbcClient.sql("""
                INSERT INTO consulta_dashboard
                    (usuario_id, equipo_id, fecha_desde,
                     fecha_hasta, resultado)
                VALUES
                    (:usuarioId, :equipoId, :desde,
                     :hasta, :resultado)
                """)
                .param("usuarioId", usuarioId)
                .param("equipoId", equipoId)
                .param("desde", desde)
                .param("hasta", hasta)
                .param("resultado", resultado)
                .update();
    }

    public record ItemAgregado(
            Long itemId,
            String tema,
            Long configuracionLdpId,
            String mecanismo,
            BigDecimal probabilidadVerdad,
            BigDecimal epsilon,
            int valorMin,
            int valorMax,
            BigDecimal suma,
            long n) {
              public ItemAgregado conSuma(BigDecimal nuevaSuma) {
        return new ItemAgregado(
                itemId,
                tema,
                configuracionLdpId,
                mecanismo,
                probabilidadVerdad,
                epsilon,
                valorMin,
                valorMax,
                nuevaSuma,
                n
        );
    }
    }
}