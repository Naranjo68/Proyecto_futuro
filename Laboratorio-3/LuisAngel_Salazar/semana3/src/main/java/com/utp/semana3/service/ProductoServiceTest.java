package main.java.com.utp.semana3.service;

import com.utp.semana3.model.Producto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoServiceTest {

    @Test
    void registrarProductoValido_debeAsignarIdYGuardar() {

        ProductoService service = new ProductoService();

        Producto producto =
                new Producto(null, "Laptop", 3500.00, 10);

        Producto registrado =
                service.registrar(producto);

        assertThat(registrado.getId()).isNotNull();

        assertThat(registrado.getNombre())
                .isEqualTo("Laptop");

        assertThat(service.listar())
                .hasSize(1);
    }

    @Test
    void registrarProductoConPrecioCero_debeLanzarExcepcion() {

        ProductoService service = new ProductoService();

        Producto producto =
                new Producto(null, "Mouse", 0.00, 5);

        assertThatThrownBy(() ->
                service.registrar(producto)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio debe ser mayor que cero");
    }

    @Test
    void registrarProductoSinNombre_debeLanzarExcepcion() {

        ProductoService service = new ProductoService();

        Producto producto =
                new Producto(null, "", 100.00, 5);

        assertThatThrownBy(() ->
                service.registrar(producto)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre es obligatorio");
    }

    @Test
    void registrarProductoConStockNegativo_debeLanzarExcepcion() {

        ProductoService service = new ProductoService();

        Producto producto =
                new Producto(null, "Teclado", 150.00, -1);

        assertThatThrownBy(() ->
                service.registrar(producto)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El stock no puede ser negativo");
    }

    // EJERCICIO 1

    @Test
    void buscarProductoExistente_debeRetornarProducto() {

        ProductoService service = new ProductoService();

        Producto producto =
                service.registrar(
                        new Producto(null, "Laptop", 3500, 10)
                );

        Optional<Producto> resultado =
                service.buscarPorId(producto.getId());

        assertThat(resultado)
                .isPresent();

        assertThat(resultado.get().getNombre())
                .isEqualTo("Laptop");
    }

    @Test
    void buscarProductoInexistente_debeRetornarVacio() {

        ProductoService service = new ProductoService();

        Optional<Producto> resultado =
                service.buscarPorId(99L);

        assertThat(resultado)
                .isEmpty();
    }

    // EJERCICIO 2

    @Test
    void eliminarProductoExistente_debeEliminarlo() {

        ProductoService service = new ProductoService();

        Producto producto =
                service.registrar(
                        new Producto(null, "Mouse", 80, 20)
                );

        boolean eliminado =
                service.eliminar(producto.getId());

        assertThat(eliminado)
                .isTrue();

        assertThat(service.buscarPorId(producto.getId()))
                .isEmpty();
    }

    @Test
    void eliminarProductoInexistente_debeRetornarFalse() {

        ProductoService service = new ProductoService();

        boolean eliminado =
                service.eliminar(99L);

        assertThat(eliminado)
                .isFalse();
    }

    // EJERCICIO 3

    @Test
    void actualizarProductoExistente_debeModificarDatos() {

        ProductoService service = new ProductoService();

        Producto producto =
                service.registrar(
                        new Producto(null, "Laptop", 3500, 10)
                );

        Producto nuevosDatos =
                new Producto(
                        null,
                        "Laptop Gamer",
                        4500,
                        5
                );

        Optional<Producto> resultado =
                service.actualizar(
                        producto.getId(),
                        nuevosDatos
                );

        assertThat(resultado)
                .isPresent();

        assertThat(resultado.get().getNombre())
                .isEqualTo("Laptop Gamer");

        assertThat(resultado.get().getPrecio())
                .isEqualTo(4500);

        assertThat(resultado.get().getStock())
                .isEqualTo(5);
    }

    @Test
    void actualizarProductoInexistente_debeRetornarVacio() {

        ProductoService service = new ProductoService();

        Producto datos =
                new Producto(
                        null,
                        "Monitor",
                        800,
                        10
                );

        Optional<Producto> resultado =
                service.actualizar(99L, datos);

        assertThat(resultado)
                .isEmpty();
    }

    @Test
    void actualizarConPrecioInvalido_debeLanzarExcepcion() {

        ProductoService service = new ProductoService();

        Producto producto =
                service.registrar(
                        new Producto(null, "Laptop", 3500, 10)
                );

        Producto datos =
                new Producto(
                        null,
                        "Laptop",
                        0,
                        10
                );

        assertThatThrownBy(() ->
                service.actualizar(producto.getId(), datos)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "El precio debe ser mayor que cero"
                );
    }
}
