package com.kala.backend.repository;

import com.kala.backend.model.Cuestionario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {

    Optional<Cuestionario> findByCodigo(String codigo);

    // Cuestionarios que la empresa tiene activos.
    @Query("""
            SELECT c
            FROM Cuestionario c
            WHERE c.id IN (
                SELECT ec.id.cuestionarioId
                FROM EmpresaCuestionario ec
                WHERE ec.id.empresaId = :empresaId AND ec.activo = true
            )
            ORDER BY c.codigo ASC
            """)
    List<Cuestionario> listarActivosDeEmpresa(@Param("empresaId") Long empresaId);
}
