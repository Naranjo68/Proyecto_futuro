package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuestionario")
public class Cuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cadencia_dias", nullable = false)
    private short cadenciaDias;

    // Cuántos ítems se envían por vez; nulo significa todos.
    @Column(name = "items_por_envio")
    private Short itemsPorEnvio;

    protected Cuestionario() {
        // Requerido por JPA.
    }

    public Cuestionario(String codigo, String nombre, short cadenciaDias, Short itemsPorEnvio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cadenciaDias = cadenciaDias;
        this.itemsPorEnvio = itemsPorEnvio;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public short getCadenciaDias() {
        return cadenciaDias;
    }

    public Short getItemsPorEnvio() {
        return itemsPorEnvio;
    }
}
