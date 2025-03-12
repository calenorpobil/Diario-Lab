package com.merlita.diariolab.Modelos;

import java.time.LocalDateTime;

public class Dato {
    private String fkTipoN;      // VARCHAR(50)
    private String fkTipoE;      // VARCHAR(50)
    private LocalDateTime fkOcurrencia; // DATETIME
    private String valorText;    // TEXT

    // Constructor
    public Dato(String fkTipoN, String fkTipoE, LocalDateTime fkOcurrencia, String valorText) {
        this.fkTipoN = fkTipoN;
        this.fkTipoE = fkTipoE;
        this.fkOcurrencia = fkOcurrencia;
        this.valorText = valorText;
    }

    // Getters y Setters
    public String getFkTipoN() {
        return fkTipoN;
    }

    public void setFkTipoN(String fkTipoN) {
        this.fkTipoN = fkTipoN;
    }

    public String getFkTipoE() {
        return fkTipoE;
    }

    public void setFkTipoE(String fkTipoE) {
        this.fkTipoE = fkTipoE;
    }

    public LocalDateTime getFkOcurrencia() {
        return fkOcurrencia;
    }

    public void setFkOcurrencia(LocalDateTime fkOcurrencia) {
        this.fkOcurrencia = fkOcurrencia;
    }

    public String getValorText() {
        return valorText;
    }

    public void setValorText(String valorText) {
        this.valorText = valorText;
    }
}