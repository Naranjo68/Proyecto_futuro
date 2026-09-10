package com.kala.backend.model;

public class Equipo {

    private final Long id;
    private String nombre;
    private Long empresaId;

    public Equipo(Long id, String nombre, Long empresaId) {
        this.id = id;
        this.nombre = nombre;
        this.empresaId = empresaId;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
}
