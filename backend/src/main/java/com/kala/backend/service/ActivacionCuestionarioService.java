package com.kala.backend.service;

import com.kala.backend.exception.CuestionarioNoEncontradoException;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.exception.PresupuestoExcedidoException;
import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.EmpresaCuestionario;
import com.kala.backend.model.EmpresaCuestionarioId;
import com.kala.backend.model.ItemCuestionario;
import com.kala.backend.model.PresupuestoPrivacidadId;
import com.kala.backend.repository.ConfiguracionLdpRepository;
import com.kala.backend.repository.CuestionarioRepository;
import com.kala.backend.repository.EmpresaCuestionarioRepository;
import com.kala.backend.repository.EmpresaRepository;
import com.kala.backend.repository.ItemCuestionarioRepository;
import com.kala.backend.repository.PresupuestoPrivacidadRepository;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivacionCuestionarioService {

    // Resultado de una activación: lo que gastaría una persona al mes con todo lo activo, y el máximo vigente.
    public record ResultadoActivacion(Long empresaId, Long cuestionarioId, double epsilonMensual, double epsilonMaximo) {
    }

    private final CalculadoraPresupuesto calculadora = new CalculadoraPresupuesto();

    private final EmpresaRepository empresas;
    private final CuestionarioRepository cuestionarios;
    private final ItemCuestionarioRepository items;
    private final ConfiguracionLdpRepository configuraciones;
    private final EmpresaCuestionarioRepository activaciones;
    private final PresupuestoPrivacidadRepository presupuestos;
    private final Clock reloj;
    private final double epsilonMaximoPorDefecto;

    public ActivacionCuestionarioService(
            EmpresaRepository empresas,
            CuestionarioRepository cuestionarios,
            ItemCuestionarioRepository items,
            ConfiguracionLdpRepository configuraciones,
            EmpresaCuestionarioRepository activaciones,
            PresupuestoPrivacidadRepository presupuestos,
            Clock reloj,
            @Value("${kala.privacidad.epsilon-maximo-persona}") double epsilonMaximoPorDefecto) {
        this.empresas = empresas;
        this.cuestionarios = cuestionarios;
        this.items = items;
        this.configuraciones = configuraciones;
        this.activaciones = activaciones;
        this.presupuestos = presupuestos;
        this.reloj = reloj;
        this.epsilonMaximoPorDefecto = epsilonMaximoPorDefecto;
    }

    // Activa el cuestionario si el ε mensual por persona (lo ya activo más el nuevo) no excede el máximo del mes.
    // Si lo excede lanza PresupuestoExcedidoException y no se escribe nada.
    @Transactional
    public ResultadoActivacion activar(Long empresaId, Long cuestionarioId) {
        if (!empresas.existsById(empresaId)) {
            throw new EmpresaNoEncontradaException(empresaId);
        }
        Cuestionario nuevo = cuestionarios.findById(cuestionarioId)
                .orElseThrow(() -> new CuestionarioNoEncontradoException(cuestionarioId));

        double gastoActual = cuestionarios.listarActivosDeEmpresa(empresaId).stream()
                .filter(activo -> !activo.getId().equals(cuestionarioId))
                .mapToDouble(this::epsilonMensualDe)
                .sum();
        double total = gastoActual + epsilonMensualDe(nuevo);
        double maximo = epsilonMaximo(empresaId);

        if (total > maximo) {
            throw new PresupuestoExcedidoException(total, maximo);
        }

        EmpresaCuestionario activacion = activaciones.findById(new EmpresaCuestionarioId(empresaId, cuestionarioId))
                .orElseGet(() -> new EmpresaCuestionario(empresaId, cuestionarioId, true));
        activacion.setActivo(true);
        activaciones.save(activacion);

        return new ResultadoActivacion(empresaId, cuestionarioId, total, maximo);
    }

    private double epsilonMaximo(Long empresaId) {
        String periodo = YearMonth.now(reloj).toString();
        return presupuestos.findById(new PresupuestoPrivacidadId(empresaId, periodo))
                .map(p -> p.getEpsilonMaximoPersona().doubleValue())
                .orElse(epsilonMaximoPorDefecto);
    }

    private double epsilonMensualDe(Cuestionario cuestionario) {
        List<Double> epsilons = items.listarPorCuestionario(cuestionario.getId()).stream()
                .map(this::epsilonDe)
                .toList();
        Integer k = cuestionario.getItemsPorEnvio() != null ? cuestionario.getItemsPorEnvio().intValue() : null;
        return calculadora.epsilonMensual(cuestionario.getCadenciaDias(), k, epsilons);
    }

    private double epsilonDe(ItemCuestionario item) {
        ConfiguracionLdp config = configuraciones.findById(item.getConfiguracionLdpId())
                .orElseThrow(() -> new IllegalStateException(
                        "El ítem " + item.getId() + " referencia una configuración inexistente"));
        return calculadora.epsilonItem(config.getMecanismo(), config.getProbabilidadVerdad(), config.getEpsilon());
    }
}
