package com.kala.backend.repository;

import com.kala.backend.model.EmpresaCuestionario;
import com.kala.backend.model.EmpresaCuestionarioId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaCuestionarioRepository extends JpaRepository<EmpresaCuestionario, EmpresaCuestionarioId> {
}
