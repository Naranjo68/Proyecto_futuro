package com.tienda.inventario.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String talla;
    private String color;
    private String categoria;
    private double precio;
    private int stock;
    private String marca;

    public Producto() {
    }

    public Producto(String nombre, String talla, String color,
                    String categoria, double precio, int stock, String marca) {
        this.nombre = nombre;
        this.talla = talla;
        this.color = color;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.marca = marca;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTalla() {
        return talla;
    }

    public String getColor() {
        return color;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public String getMarca() {
        return marca;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }
}