package com.merlita.diariolab.Modelos;

import java.time.LocalDateTime;

public class Ocurrencia {
    private LocalDateTime fecha;  // DATETIME
    private String fkEstudioN;   // VARCHAR(50)

    // Constructor
    public Ocurrencia(LocalDateTime fecha, String fkEstudioN) {
        this.fecha = fecha;
        this.fkEstudioN = fkEstudioN;
    }

    // Getters y Setters
    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getFkEstudioN() {
        return fkEstudioN;
    }

    public void setFkEstudioN(String fkEstudioN) {
        this.fkEstudioN = fkEstudioN;
    }
}