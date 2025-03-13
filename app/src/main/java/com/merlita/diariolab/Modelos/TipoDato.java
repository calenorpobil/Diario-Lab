package com.merlita.diariolab.Modelos;

public class TipoDato {
    private String nombre;       // VARCHAR(20)
    private String tipoDato;     // VARCHAR(20)
    private String descripcion;  // VARCHAR(100)
    private String fkEstudio;    // NVARCHAR(50)

    // Constructor
    public TipoDato(String nombre, String tipoDato, String descripcion, String fkEstudio) {
        this.nombre = nombre;
        this.tipoDato = tipoDato;
        this.descripcion = descripcion;
        this.fkEstudio = fkEstudio;
    }

    public TipoDato() {
        this.nombre="";
        this.tipoDato="";
        this.descripcion="";
        this.fkEstudio = "";
        //En el alta, el estudio se añadirá al guardar el estudio.
    }

    public TipoDato(String nombre, String tipoDato, String descripcion) {
        this.nombre = nombre;
        this.tipoDato = tipoDato;
        this.descripcion = descripcion;
        this.fkEstudio = "";
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFkEstudio() {
        return fkEstudio;
    }

    public void setFkEstudio(String fkEstudio) {
        this.fkEstudio = fkEstudio;
    }
}