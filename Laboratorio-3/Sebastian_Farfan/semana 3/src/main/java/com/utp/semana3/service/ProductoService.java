package com.utp.semana3.service;
import com.utp.semana3.model.Producto;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
@Service
public class ProductoService {
 private final List<Producto> productos = new ArrayList<>();
 private long secuencia = 1;
public Producto registrar(Producto producto) {
 if (producto.getPrecio() <= 0) {
 throw new IllegalArgumentException("El precio debe ser mayor que cero");
 }
 producto.setId(secuencia++);
 productos.add(producto);
 return producto;
}

 public List<Producto> listar() {
 return productos;
 }
 
}