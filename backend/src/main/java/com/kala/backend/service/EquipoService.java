package com.kala.backend.service;

import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.Equipo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Lógica de negocio de equipos. Almacenamiento en memoria (Avance 1).
 *
 * No inyecta {@code EmpresaService}: la validación "la empresa debe existir
 * de verdad" se difiere a integración (#25). Aquí solo se valida que el
 * {@code empresaId} y el {@code nombre} vengan informados.
 */
@Service
public class EquipoService {

    private final Map<Long, Equipo> equipos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public List<Equipo> listarPorEmpresa(Long empresaId) {
        return equipos.values().stream()
                .filter(equipo -> empresaId == null || empresaId.equals(equipo.getEmpresaId()))
                .toList();
    }

    public Equipo buscarPorId(Long id) {
        Equipo equipo = equipos.get(id);
        if (equipo == null) {
            throw new EquipoNoEncontradoException(id);
        }
        return equipo;
    }

    public Equipo crear(EquipoRequest request) {
        validar(request);
        Long id = secuencia.incrementAndGet();
        Equipo equipo = new Equipo(id, request.nombre(), request.empresaId());
        equipos.put(id, equipo);
        return equipo;
    }

    public Equipo actualizar(Long id, EquipoRequest request) {
        validar(request);
        Equipo equipo = buscarPorId(id);
        equipo.setNombre(request.nombre());
        equipo.setEmpresaId(request.empresaId());
        return equipo;
    }

    public void eliminar(Long id) {
        if (equipos.remove(id) == null) {
            throw new EquipoNoEncontradoException(id);
        }
    }

    private void validar(EquipoRequest request) {
        if (request.empresaId() == null) {
            throw new IllegalArgumentException("El equipo debe pertenecer a una empresa (empresaId es obligatorio).");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio.");
        }
    }
}
