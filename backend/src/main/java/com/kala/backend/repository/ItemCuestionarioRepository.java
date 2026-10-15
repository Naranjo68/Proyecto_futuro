package com.kala.backend.repository;

import com.kala.backend.model.ItemCuestionario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemCuestionarioRepository extends JpaRepository<ItemCuestionario, Long> {

    @Query("""
            SELECT i
            FROM ItemCuestionario i
            WHERE i.cuestionarioId = :cuestionarioId
            ORDER BY i.orden ASC
            """)
    List<ItemCuestionario> listarPorCuestionario(@Param("cuestionarioId") Long cuestionarioId);
}
