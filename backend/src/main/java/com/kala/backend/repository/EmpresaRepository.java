
package com.kala.backend.repository;

import com.kala.backend.model.Empresa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    @Query("""
            SELECT e
            FROM Empresa e
            WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY e.nombre ASC
            """)
    List<Empresa> buscarPorNombre(@Param("texto") String texto);

    @Query("""
            SELECT e
            FROM Empresa e
            WHERE e.id = :empresaId
              AND LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY e.nombre ASC
            """)
    List<Empresa> buscarPorNombreEnEmpresa(
            @Param("empresaId") Long empresaId,
            @Param("texto") String texto);
}
