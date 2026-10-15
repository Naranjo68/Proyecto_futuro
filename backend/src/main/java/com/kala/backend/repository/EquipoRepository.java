
package com.kala.backend.repository;

import com.kala.backend.model.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByEmpresaId(Long empresaId);

    boolean existsByEmpresaId(Long empresaId);
}
