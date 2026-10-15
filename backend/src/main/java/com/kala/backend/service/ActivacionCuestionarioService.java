package com.kala.backend.service;

import com.kala.backend.exception.PresupuestoExcedidoException;
import com.kala.backend.model.*;
import com.kala.backend.repository.ActivacionCuestionarioRepository;
import com.kala.backend.repository.CuestionarioRepository;
import com.kala.backend.repository.PresupuestoPrivacidadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ActivacionCuestionarioService {

    private final ActivacionCuestionarioRepository activacionRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final PresupuestoPrivacidadRepository presupuestoRepository;
    private final CalculadoraPresupuesto calculadora;
    private final double epsilonMaximoDefault;

    public ActivacionCuestionarioService(
            ActivacionCuestionarioRepository activacionRepository,
            CuestionarioRepository cuestionarioRepository,
            PresupuestoPrivacidadRepository presupuestoRepository,
            CalculadoraPresupuesto calculadora,
            @Value("${kala.privacidad.epsilon-maximo-persona:10.0}") double epsilonMaximoDefault) {
        this.activacionRepository = activacionRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.calculadora = calculadora;
        this.epsilonMaximoDefault = epsilonMaximoDefault;
    }

    @Transactional
    public void activar(Long empresaId, Long cuestionarioId) {
        Cuestionario cuestionarioANuevo = cuestionarioRepository.findById(cuestionarioId)
                .orElseThrow(() -> new IllegalArgumentException("Cuestionario no encontrado: " + cuestionarioId));

        List<Cuestionario> cuestionariosActivos = activacionRepository.findCuestionariosActivosByEmpresaId(empresaId);

        double epsilonAcumulado = cuestionariosActivos.stream()
                .mapToDouble(calculadora::calcularEpsilonMensualCuestionario)
                .sum();

        double epsilonNuevo = calculadora.calcularEpsilonMensualCuestionario(cuestionarioANuevo);
        double epsilonTotal = epsilonAcumulado + epsilonNuevo;

        String periodoActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        double maximoPermitido = presupuestoRepository.findById(new PresupuestoPrivacidadId(empresaId, periodoActual))
                .map(p -> p.getEpsilonMaximoPersona().doubleValue())
                .orElse(epsilonMaximoDefault);

        if (epsilonTotal > maximoPermitido) {
            throw new PresupuestoExcedidoException(empresaId, epsilonTotal, maximoPermitido);
        }

        EmpresaCuestionario activacion = new EmpresaCuestionario(empresaId, cuestionarioANuevo, true);
        activacionRepository.save(activacion);
    }
}
