package com.kala.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuestionario")
public class Cuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "cadencia_dias", nullable = false)
    private Short cadenciaDias;

    @Column(name = "items_por_envio")
    private Short itemsPorEnvio;

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCuestionario> items = new ArrayList<>();

    public Cuestionario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Short getCadenciaDias() { return cadenciaDias; }
    public void setCadenciaDias(Short cadenciaDias) { this.cadenciaDias = cadenciaDias; }

    public Short getItemsPorEnvio() { return itemsPorEnvio; }
    public void setItemsPorEnvio(Short itemsPorEnvio) { this.itemsPorEnvio = itemsPorEnvio; }

    public List<ItemCuestionario> getItems() { return items; }
    public void setItems(List<ItemCuestionario> items) { this.items = items; }
}
