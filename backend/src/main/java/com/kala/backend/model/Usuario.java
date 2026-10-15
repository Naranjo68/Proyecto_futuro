package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// Credenciales comunes de toda persona que entra al sistema. El rol no se guarda aquí:
// se deriva de los subtipos (empleado, personal de empresa, administrador de plataforma).
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private boolean activo = true;

    // La asigna la base (DEFAULT CURRENT_TIMESTAMP); desde Java solo se lee.
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    protected Usuario() {
    }

    public Usuario(String email, String passwordHash, String nombre) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
