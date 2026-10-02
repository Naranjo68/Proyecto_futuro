package main.java.com.utp.semana6.repository;

import org.springframework.data.jpa.repository.JpaRepository; 
import com.utp.productosapi.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
}
