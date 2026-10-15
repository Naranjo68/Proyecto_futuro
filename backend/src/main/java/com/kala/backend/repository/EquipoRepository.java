
package com.kala.backend.repository;

import com.kala.backend.model.Equipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByEmpresaId(Long empresaId);

    Optional<Equipo> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByEmpresaId(Long empresaId);

    @Query("""
            SELECT e
            FROM Equipo e
            WHERE e.empresaId = :empresaId
            ORDER BY e.nombre ASC
            """)
    List<Equipo> listarPorEmpresaOrdenado(
            @Param("empresaId") Long empresaId);

    @Query("""
            SELECT COUNT(e)
            FROM Equipo e
            WHERE e.empresaId = :empresaId
            """)
    long contarPorEmpresa(@Param("empresaId") Long empresaId);
}
