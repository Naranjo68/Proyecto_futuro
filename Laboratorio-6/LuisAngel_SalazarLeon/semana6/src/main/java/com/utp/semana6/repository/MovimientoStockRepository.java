package com.utp.semana6.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.semana6.model.MovimientoStock;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    
}
