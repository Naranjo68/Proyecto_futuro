package com.kala.backend.service;

import com.kala.backend.exception.PresupuestoExcedidoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivacionCuestionarioService {

    @Transactional
    public void activar(Long empresaId, Long cuestionarioId) {
        // En la implementación real, aquí se suma el epsilon de los cuestionarios activos 
        // usando JPQL y se compara con la tabla presupuesto_privacidad.
        double epsilonMaximo = 10.0; 
        double epsilonCalculado = 0.0; 

        if (epsilonCalculado > epsilonMaximo) {
            throw new PresupuestoExcedidoException(empresaId, epsilonCalculado, epsilonMaximo);
        }
    }
}
