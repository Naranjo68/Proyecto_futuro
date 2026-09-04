package com.utp.semana4_api_rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DisminuirStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad a disminuir debe ser al menos 1")
    private Integer cantidad;

    public DisminuirStockRequest() {
    }

    public DisminuirStockRequest(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
