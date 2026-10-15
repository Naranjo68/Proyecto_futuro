
package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "rubro", nullable = false, length = 100)
    private String rubro;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    protected Empresa() {
        // Constructor requerido por JPA.
    }

    public Empresa(Long id, String nombre, String rubro) {
        this.id = id;
        this.nombre = nombre;
        this.rubro = rubro;
    }

    public Empresa(String ruc, String nombre, String rubro) {
        this.ruc = ruc;
        this.nombre = nombre;
        this.rubro = rubro;
    }

    public Long getId() {
        return id;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
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

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    @PrePersist
    protected void alCrear() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }
}
