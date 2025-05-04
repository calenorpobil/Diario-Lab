package com.merlita.diariolab.Modelos;

public class Cualitativo {
    private String titulo, fk_dato_tipo_e, fk_dato_tipo_t;

    public Cualitativo(String titulo, String fk_dato_tipo_e, String fk_dato_tipo_t) {
        this.titulo = titulo;
        this.fk_dato_tipo_e = fk_dato_tipo_e;
        this.fk_dato_tipo_t = fk_dato_tipo_t;
    }

    public Cualitativo() {

    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFk_dato_tipo_e() {
        return fk_dato_tipo_e;
    }

    public void setFk_dato_tipo_e(String fk_dato_tipo_e) {
        this.fk_dato_tipo_e = fk_dato_tipo_e;
    }

    public String getFk_dato_tipo_t() {
        return fk_dato_tipo_t;
    }

    public void setFk_dato_tipo_t(String fk_dato_tipo_t) {
        this.fk_dato_tipo_t = fk_dato_tipo_t;
    }
}
