package com.kala.backend.controller;

import com.kala.backend.dto.EmpresaConEquiposRequest;
import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.model.Empresa;
import com.kala.backend.service.EmpresaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Empresa> listar() {
        return service.listar();
    }

    @GetMapping("/buscar")
    public List<Empresa> buscarPorNombre(
            @RequestParam("texto") String texto) {

        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El texto de busqueda no puede estar vacio"
            );
        }

        return service.buscarPorNombre(texto);
    }

    @GetMapping("/{id}")
    public Empresa buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Empresa> crear(
            @Valid @RequestBody EmpresaRequest request) {

        Empresa creada = service.crear(request);

        return ResponseEntity
                .created(URI.create("/api/empresas/" + creada.getId()))
                .body(creada);
    }

    @PostMapping("/con-equipos")
    public ResponseEntity<Empresa> crearConEquipos(
            @Valid @RequestBody EmpresaConEquiposRequest request) {

        Empresa creada = service.crearConEquipos(request);

        return ResponseEntity
                .created(URI.create("/api/empresas/" + creada.getId()))
                .body(creada);
    }

    @PutMapping("/{id}")
    public Empresa actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaRequest request) {

        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}