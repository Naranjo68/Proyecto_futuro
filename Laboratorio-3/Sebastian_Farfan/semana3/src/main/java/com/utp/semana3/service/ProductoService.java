package main.java.com.utp.semana3.service;

import com.utp.semana3.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final List<Producto> productos = new ArrayList<>();

    private long secuencia = 1;

    // Registrar producto
    public Producto registrar(Producto producto) {

        validar(producto);

        producto.setId(secuencia++);

        productos.add(producto);

        return producto;
    }

    // Listar productos
    public List<Producto> listar() {
        return productos;
    }

    // Buscar por ID
    public Optional<Producto> buscarPorId(Long id) {

        return productos.stream()
                .filter(producto -> producto.getId().equals(id))
                .findFirst();
    }

    // Eliminar producto
    public boolean eliminar(Long id) {

        Optional<Producto> producto = buscarPorId(id);

        if (producto.isPresent()) {
            productos.remove(producto.get());
            return true;
        }

        return false;
    }

    // Actualizar producto
    public Optional<Producto> actualizar(Long id, Producto datos) {

        Optional<Producto> productoEncontrado = buscarPorId(id);

        if (productoEncontrado.isEmpty()) {
            return Optional.empty();
        }

        validar(datos);

        Producto producto = productoEncontrado.get();

        producto.setNombre(datos.getNombre());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());

        return Optional.of(producto);
    }

    // Validaciones
    private void validar(Producto producto) {

        if (producto.getNombre() == null ||
                producto.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        if (producto.getPrecio() <= 0) {

            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero"
            );
        }

        if (producto.getStock() < 0) {

            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }
    }
}