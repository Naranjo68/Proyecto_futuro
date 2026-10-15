package com.kala.backend.service;

import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.model.Empresa;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {

    // Índice de empresas por id. AtomicLong genera ids incrementales.
    private final Map<Long, Empresa> empresas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public List<Empresa> listar() {
        return List.copyOf(empresas.values());
    }

    // Único método que lanza el 404; lo reutilizan actualizar y eliminar.
    public Empresa buscarPorId(Long id) {
        Empresa empresa = empresas.get(id);
        if (empresa == null) {
            throw new EmpresaNoEncontradaException(id);
        }
        return empresa;
    }

    public Empresa crear(EmpresaRequest request) {
        Long id = secuencia.incrementAndGet();
        Empresa empresa = new Empresa(id, request.nombre(), request.rubro());
        empresas.put(id, empresa);
        return empresa;
    }

    public Empresa actualizar(Long id, EmpresaRequest request) {
        Empresa empresa = buscarPorId(id);
        empresa.setNombre(request.nombre());
        empresa.setRubro(request.rubro());
        return empresa;
    }

    public void eliminar(Long id) {
        buscarPorId(id);            // 404 si no existe
        empresas.remove(id);
    }
}
