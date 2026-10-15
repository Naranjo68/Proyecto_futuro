package com.kala.backend.repository;

import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.EmpresaCuestionario;
import com.kala.backend.model.EmpresaCuestionarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivacionCuestionarioRepository extends JpaRepository<EmpresaCuestionario, EmpresaCuestionarioId> {

    @Query("SELECT ec.cuestionario FROM EmpresaCuestionario ec WHERE ec.id.empresaId = :empresaId AND ec.activo = true")
    List<Cuestionario> findCuestionariosActivosByEmpresaId(@Param("empresaId") Long empresaId);
}
