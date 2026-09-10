package com.kala.backend.model;

public class Empresa {

    private final Long id;
    private String nombre;
    private String rubro;

    public Empresa(Long id, String nombre, String rubro) {
        this.id = id;
        this.nombre = nombre;
        this.rubro = rubro;
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

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }
}
