package com.kala.backend.controller;

import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.model.Empresa;
import com.kala.backend.service.EmpresaService;
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
import org.springframework.web.bind.annotation.RestController;

// Recibe las peticiones HTTP y delega en el service. Sin lógica ni try/catch.
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

    @GetMapping("/{id}")
    public Empresa buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // 201 Created + header Location apuntando a la empresa recién creada.
    @PostMapping
    public ResponseEntity<Empresa> crear(@Valid @RequestBody EmpresaRequest request) {
        Empresa creada = service.crear(request);
        return ResponseEntity.created(URI.create("/api/empresas/" + creada.getId())).body(creada);
    }

    @PutMapping("/{id}")
    public Empresa actualizar(@PathVariable Long id, @Valid @RequestBody EmpresaRequest request) {
        return service.actualizar(id, request);
    }

    // 204 No Content: se eliminó y no hay cuerpo que devolver.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
