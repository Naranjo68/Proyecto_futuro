package main.java.com.utp.semana3.controller;


import com.utp.semana3.model.Producto;
import com.utp.semana3.service.ProductoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /productos
    @GetMapping
    public List<Producto> listar() {

        return productoService.listar();
    }

    // GET /productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(
            @PathVariable Long id) {

        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    // POST /productos
    @PostMapping
    public ResponseEntity<Producto> registrar(
            @RequestBody Producto producto) {

        Producto registrado =
                productoService.registrar(producto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrado);
    }

    // DELETE /productos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        boolean eliminado =
                productoService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .noContent()
                .build();
    }

    // PUT /productos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @RequestBody Producto producto) {

        return productoService
                .actualizar(id, producto)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }
}