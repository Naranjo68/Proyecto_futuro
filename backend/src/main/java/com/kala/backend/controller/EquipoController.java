package com.kala.backend.controller;

import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.model.Equipo;
import com.kala.backend.service.EquipoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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

// Traduce las peticiones HTTP a llamadas del service. Sin lógica de negocio ni try/catch.
@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService service;

    public EquipoController(EquipoService service) {
        this.service = service;
    }

    // required = false -> el filtro por empresa es opcional.
    @GetMapping
    public List<Equipo> listar(@RequestParam(required = false) Long empresaId) {
        return service.listarPorEmpresa(empresaId);
    }

    @GetMapping("/{id}")
    public Equipo buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // 201 Created + header Location con la URL del recurso creado.
    @PostMapping
    public ResponseEntity<Equipo> crear(@Valid @RequestBody EquipoRequest request) {
        Equipo creado = service.crear(request);
        return ResponseEntity.created(URI.create("/api/equipos/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public Equipo actualizar(@PathVariable Long id, @Valid @RequestBody EquipoRequest request) {
        return service.actualizar(id, request);
    }

    // 204 No Content: se eliminó y no hay cuerpo que devolver.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
