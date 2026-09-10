package com.kala.backend.service;

import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.Equipo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class EquipoService {

    // Índice de equipos por id. AtomicLong genera ids incrementales.
    private final Map<Long, Equipo> equipos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    // Se usa para comprobar que la empresa exista antes de crear/actualizar.
    private final EmpresaService empresaService;

    public EquipoService(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    // Sin empresaId devuelve todos; con empresaId filtra por esa empresa.
    public List<Equipo> listarPorEmpresa(Long empresaId) {
        if (empresaId == null) {
            return List.copyOf(equipos.values());
        }
        return equipos.values().stream()
                .filter(equipo -> empresaId.equals(equipo.getEmpresaId()))
                .toList();
    }

    // Único método que lanza el 404; lo reutilizan actualizar y eliminar.
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
        buscarPorId(id);            // 404 si no existe
        equipos.remove(id);
    }

    // Comprobaciones comunes a crear y actualizar: campos presentes y empresa existente.
    private void validar(EquipoRequest request) {
        if (request.empresaId() == null) {
            throw new IllegalArgumentException("El equipo debe pertenecer a una empresa (empresaId es obligatorio).");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio.");
        }
        empresaService.buscarPorId(request.empresaId());   // 404 si la empresa no existe
    }
}
