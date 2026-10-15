package com.kala.backend.repository;

import com.kala.backend.model.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {
    Optional<Cuestionario> findByCodigo(String codigo);
}
