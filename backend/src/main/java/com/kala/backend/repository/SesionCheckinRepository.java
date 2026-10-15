package com.kala.backend.repository;

import com.kala.backend.model.SesionCheckin;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SesionCheckinRepository extends JpaRepository<SesionCheckin, String> {

    // Atómico y sin carreras: devuelve 1 si insertó y 0 si el hash ya existía (envío duplicado).
    // No se usa save(): con un id asignado, save() haría un UPDATE silencioso en vez de fallar.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO sesion_checkin (session_hash, cuestionario_id, ventana_inicio)
            VALUES (:hash, :cuestionarioId, :ventanaInicio)
            ON CONFLICT (session_hash) DO NOTHING
            """, nativeQuery = true)
    int insertarSiNoExiste(@Param("hash") String hash,
                           @Param("cuestionarioId") Long cuestionarioId,
                           @Param("ventanaInicio") LocalDate ventanaInicio);
}
