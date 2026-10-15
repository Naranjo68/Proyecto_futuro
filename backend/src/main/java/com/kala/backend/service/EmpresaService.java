
package com.kala.backend.service;

import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.exception.EmpresaConEquiposException;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.model.Empresa;
import com.kala.backend.repository.EmpresaRepository;
import com.kala.backend.repository.EquipoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EquipoRepository equipoRepository;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            EquipoRepository equipoRepository) {
        this.empresaRepository = empresaRepository;
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNoEncontradaException(id));
    }

    @Transactional
    public Empresa crear(EmpresaRequest request) {
        Empresa empresa = new Empresa(
                request.ruc(),
                request.nombre(),
                request.rubro()
        );

        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa actualizar(Long id, EmpresaRequest request) {
        Empresa empresa = buscarPorId(id);

        empresa.setRuc(request.ruc());
        empresa.setNombre(request.nombre());
        empresa.setRubro(request.rubro());

        return empresaRepository.save(empresa);
    }

    @Transactional
    public void eliminar(Long id) {
        Empresa empresa = buscarPorId(id);

        if (equipoRepository.existsByEmpresaId(id)) {
            throw new EmpresaConEquiposException(id);
        }

        empresaRepository.delete(empresa);
    }
}
