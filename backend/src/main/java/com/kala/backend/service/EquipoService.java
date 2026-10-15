
package com.kala.backend.service;

import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.Equipo;
import com.kala.backend.repository.EquipoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final EmpresaService empresaService;

    public EquipoService(
            EquipoRepository equipoRepository,
            EmpresaService empresaService) {
        this.equipoRepository = equipoRepository;
        this.empresaService = empresaService;
    }

    @Transactional(readOnly = true)
    public List<Equipo> listarPorEmpresa(Long empresaId) {
        if (empresaId == null) {
            return equipoRepository.findAll();
        }

        return equipoRepository.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public Equipo buscarPorId(Long id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoNoEncontradoException(id));
    }

    @Transactional
    public Equipo crear(EquipoRequest request) {
        validar(request);

        Equipo equipo = new Equipo(
                request.nombre(),
                request.empresaId()
        );

        return equipoRepository.save(equipo);
    }

    @Transactional
    public Equipo actualizar(Long id, EquipoRequest request) {
        validar(request);

        Equipo equipo = buscarPorId(id);
        equipo.setNombre(request.nombre());
        equipo.setEmpresaId(request.empresaId());

        return equipoRepository.save(equipo);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = buscarPorId(id);
        equipoRepository.delete(equipo);
    }

    private void validar(EquipoRequest request) {
        if (request.empresaId() == null) {
            throw new IllegalArgumentException(
                    "empresaId es obligatorio"
            );
        }

        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del equipo es obligatorio"
            );
        }

        empresaService.buscarPorId(request.empresaId());
    }
}
