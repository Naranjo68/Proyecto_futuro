package com.kala.backend.service;

import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.exception.SemanaInvalidaException;
import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.ConsultaDashboard;
import com.kala.backend.repository.ConsultaDashboardRepository;
import com.kala.backend.repository.DashboardRepository;
import com.kala.backend.repository.DashboardRepository.FilaItem;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    public static final String OK = "OK";
    public static final String DATOS_INSUFICIENTES = "DATOS_INSUFICIENTES";

    // Estimación de un ítem. proporcionEstimada y margenError95 son nulos si el ítem no alcanza el n mínimo.
    public record ItemEstimado(Long itemId, String tema, Double proporcionEstimada, Double margenError95) {
    }

    // totalRespuestas es el mayor n entre los ítems de la ventana (0 si no hay respuestas).
    public record ResultadoDashboard(Long equipoId, LocalDate semanaInicio, String resultado,
                                     long totalRespuestas, List<ItemEstimado> items) {
    }

    private final DashboardRepository repositorio;
    private final ConsultaDashboardRepository auditoria;
    private final int nMinimo;

    public DashboardService(
            DashboardRepository repositorio,
            ConsultaDashboardRepository auditoria,
            @Value("${kala.privacidad.n-minimo}") int nMinimo) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
        this.nMinimo = nMinimo;
    }

    // Una sola ventana semanal. Toda consulta válida queda auditada en la misma transacción, con datos
    // suficientes o sin ellos. Una semana inválida o un equipo ajeno se rechazan antes de auditar.
    @Transactional
    public ResultadoDashboard consultar(Long usuarioId, Long empresaId, Long equipoId, LocalDate semana) {
        if (semana.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new SemanaInvalidaException(semana);
        }
        if (!repositorio.equipoPerteneceAEmpresa(equipoId, empresaId)) {
            throw new EquipoNoEncontradoException(equipoId);
        }

        LocalDate hasta = semana.plusDays(6);
        List<FilaItem> filas = repositorio.agregadosDeVentana(equipoId, semana, hasta);

        List<ItemEstimado> items = filas.stream().map(this::estimar).toList();
        long total = filas.stream().mapToLong(FilaItem::n).max().orElse(0L);
        boolean hayDatosSuficientes = filas.stream().anyMatch(f -> f.n() >= nMinimo);
        String resultado = hayDatosSuficientes ? OK : DATOS_INSUFICIENTES;

        auditoria.save(new ConsultaDashboard(usuarioId, equipoId, semana, hasta, resultado));
        return new ResultadoDashboard(equipoId, semana, resultado, total, items);
    }

    private ItemEstimado estimar(FilaItem fila) {
        boolean reportable = fila.n() >= nMinimo
                && ConfiguracionLdp.RANDOMIZED_RESPONSE.equals(fila.mecanismo());
        if (!reportable) {
            return new ItemEstimado(fila.itemId(), fila.tema(), null, null);
        }
        int si = fila.suma().setScale(0, RoundingMode.HALF_UP).intValueExact();
        int n = Math.toIntExact(fila.n());
        EstimadorDesesgado estimador = new EstimadorDesesgado(fila.probabilidadVerdad().doubleValue());
        return new ItemEstimado(fila.itemId(), fila.tema(),
                estimador.estimarProporcion(si, n), estimador.margenError95(si, n));
    }
}
