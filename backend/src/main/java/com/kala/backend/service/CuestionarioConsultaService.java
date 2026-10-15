package com.kala.backend.service;

import com.kala.backend.dto.CuestionarioPendienteResponse;
import com.kala.backend.dto.CuestionarioPendienteResponse.Escala;
import com.kala.backend.dto.CuestionarioPendienteResponse.ItemPendiente;
import com.kala.backend.dto.CuestionarioPendienteResponse.Opcion;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.EscalaRespuesta;
import com.kala.backend.model.ItemCuestionario;
import com.kala.backend.repository.ConfiguracionLdpRepository;
import com.kala.backend.repository.CuestionarioRepository;
import com.kala.backend.repository.EmpresaRepository;
import com.kala.backend.repository.EscalaRespuestaRepository;
import com.kala.backend.repository.ItemCuestionarioRepository;
import com.kala.backend.repository.OpcionEscalaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CuestionarioConsultaService {

    private final EmpresaRepository empresas;
    private final CuestionarioRepository cuestionarios;
    private final ItemCuestionarioRepository items;
    private final EscalaRespuestaRepository escalas;
    private final OpcionEscalaRepository opciones;
    private final ConfiguracionLdpRepository configuraciones;

    public CuestionarioConsultaService(
            EmpresaRepository empresas,
            CuestionarioRepository cuestionarios,
            ItemCuestionarioRepository items,
            EscalaRespuestaRepository escalas,
            OpcionEscalaRepository opciones,
            ConfiguracionLdpRepository configuraciones) {
        this.empresas = empresas;
        this.cuestionarios = cuestionarios;
        this.items = items;
        this.escalas = escalas;
        this.opciones = opciones;
        this.configuraciones = configuraciones;
    }

    public List<Cuestionario> listarActivos(Long empresaId) {
        verificarEmpresa(empresaId);
        return cuestionarios.listarActivosDeEmpresa(empresaId);
    }

    // Cuestionarios activos de la empresa con sus ítems, escala y configuración LDP.
    public List<CuestionarioPendienteResponse> pendientes(Long empresaId) {
        verificarEmpresa(empresaId);
        return cuestionarios.listarActivosDeEmpresa(empresaId).stream()
                .map(this::conItems)
                .toList();
    }

    private void verificarEmpresa(Long empresaId) {
        if (!empresas.existsById(empresaId)) {
            throw new EmpresaNoEncontradaException(empresaId);
        }
    }

    private CuestionarioPendienteResponse conItems(Cuestionario c) {
        List<ItemPendiente> itemsDto = items.listarPorCuestionario(c.getId()).stream()
                .map(this::itemPendiente)
                .toList();
        return new CuestionarioPendienteResponse(c.getId(), c.getCodigo(), c.getNombre(), c.getCadenciaDias(),
                c.getItemsPorEnvio() == null ? null : c.getItemsPorEnvio().intValue(), itemsDto);
    }

    private ItemPendiente itemPendiente(ItemCuestionario item) {
        EscalaRespuesta escala = escalas.findById(item.getEscalaId()).orElseThrow();
        ConfiguracionLdp config = configuraciones.findById(item.getConfiguracionLdpId()).orElseThrow();
        List<Opcion> opcionesDto = opciones.listarPorEscala(escala.getId()).stream()
                .map(o -> new Opcion(o.getId().getValor(), o.getEtiqueta()))
                .toList();
        return new ItemPendiente(item.getId(), item.getOrden(), item.getTema(), item.getTexto(),
                new Escala(escala.getCodigo(), escala.getValorMin(), escala.getValorMax(), opcionesDto),
                config.getId(), config.getMecanismo(), config.getProbabilidadVerdad());
    }
}
