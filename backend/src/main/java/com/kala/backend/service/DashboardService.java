
package com.kala.backend.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import com.kala.backend.exception.SemanaInvalidaException;
import com.kala.backend.repository.DashboardRepository;
import com.kala.backend.repository.DashboardRepository.ItemAgregado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {

    private final DashboardRepository repository;
    private final EstimadorDesesgado estimador;
    private final int nMinimo;

    public DashboardService(
            DashboardRepository repository,
            EstimadorDesesgado estimador,
            @Value("${kala.privacidad.n-minimo:30}") int nMinimo) {

        if (nMinimo < 1) {
            throw new IllegalArgumentException(
                    "El mínimo de privacidad debe ser mayor que cero.");
        }

        this.repository = repository;
        this.estimador = estimador;
        this.nMinimo = nMinimo;
    }

    @Transactional
    public DashboardResultado consultar(
            Long usuarioId,
            Long empresaId,
            Long equipoId,
            LocalDate semana) {

        validarSemana(semana);

        if (usuarioId == null || empresaId == null || equipoId == null) {
            throw new IllegalArgumentException(
                    "Los identificadores son obligatorios.");
        }

        boolean pertenece = repository.equipoPerteneceAEmpresa(
                equipoId, empresaId);

        if (!pertenece) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró el equipo en la empresa indicada.");
        }

        LocalDate desde = semana;
        LocalDate hasta = semana.plusDays(6);

        List<ItemAgregado> agregados =
                repository.consultar(equipoId, desde, hasta);

        List<ItemResultado> items = agregados.stream()
                .map(this::calcularResultado)
                .toList();

        boolean hayInsuficientes = items.stream()
                .anyMatch(item ->
                        item.resultado().equals("DATOS_INSUFICIENTES"));

        String resultadoAuditoria = hayInsuficientes
                ? "DATOS_INSUFICIENTES"
                : "OK";

        // Se registra tanto cuando hay datos suficientes
        // como cuando el mínimo de privacidad no se alcanza.
        repository.registrarConsulta(
                usuarioId,
                equipoId,
                desde,
                hasta,
                resultadoAuditoria);

        return new DashboardResultado(
                desde,
                hasta,
                resultadoAuditoria,
                items);
    }

    private void validarSemana(LocalDate semana) {
        if (semana == null || semana.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new SemanaInvalidaException();
        }
    }

    private ItemResultado calcularResultado(ItemAgregado item) {
        long n = item.n();

        if (n < nMinimo) {
            return new ItemResultado(
                    item.itemId(),
                    item.tema(),
                    n,
                    "DATOS_INSUFICIENTES",
                    null,
                    null);
        }

        if (n > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "El total de respuestas supera el rango del estimador.");
        }

        int respuestasSi;
        try {
            respuestasSi = item.suma().intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalStateException(
                    "La suma reportada debe ser un número entero.", ex);
        }

        int total = Math.toIntExact(n);

        double proporcion = estimador.estimarProporcion(
                respuestasSi, total);

        double margen = estimador.margenError95(
                respuestasSi, total);

        return new ItemResultado(
                item.itemId(),
                item.tema(),
                n,
                "OK",
                proporcion,
                margen);
    }

    public record DashboardResultado(
            LocalDate desde,
            LocalDate hasta,
            String resultado,
            List<ItemResultado> items) {
    }

    public record ItemResultado(
            Long itemId,
            String tema,
            long totalRespuestas,
            String resultado,
            Double proporcionEstimada,
            Double margenError95) {
    }
}