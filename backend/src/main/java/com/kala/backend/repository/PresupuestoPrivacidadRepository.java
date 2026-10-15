package com.kala.backend.repository;

import com.kala.backend.model.PresupuestoPrivacidad;
import com.kala.backend.model.PresupuestoPrivacidadId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresupuestoPrivacidadRepository extends JpaRepository<PresupuestoPrivacidad, PresupuestoPrivacidadId> {
}
