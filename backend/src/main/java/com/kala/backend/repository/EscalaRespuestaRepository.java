package com.kala.backend.repository;

import com.kala.backend.model.EscalaRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EscalaRespuestaRepository extends JpaRepository<EscalaRespuesta, Long> {
    Optional<EscalaRespuesta> findByCodigo(String codigo);
}
