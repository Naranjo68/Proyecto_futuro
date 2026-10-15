package com.kala.backend.repository;

import com.kala.backend.model.OpcionEscala;
import com.kala.backend.model.OpcionEscalaId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OpcionEscalaRepository extends JpaRepository<OpcionEscala, OpcionEscalaId> {

    @Query("""
            SELECT o
            FROM OpcionEscala o
            WHERE o.id.escalaId = :escalaId
            ORDER BY o.id.valor ASC
            """)
    List<OpcionEscala> listarPorEscala(@Param("escalaId") Long escalaId);
}
