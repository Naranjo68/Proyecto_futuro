package com.kala.backend.repository;

import com.kala.backend.model.ConfiguracionLdp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfiguracionLdpRepository extends JpaRepository<ConfiguracionLdp, Long> {

    // La configuración vigente de un ítem es la que el ítem referencia hoy.
    @Query("""
            SELECT c
            FROM ConfiguracionLdp c
            WHERE c.id = (SELECT i.configuracionLdpId FROM ItemCuestionario i WHERE i.id = :itemId)
            """)
    Optional<ConfiguracionLdp> buscarVigenteDeItem(@Param("itemId") Long itemId);
}
