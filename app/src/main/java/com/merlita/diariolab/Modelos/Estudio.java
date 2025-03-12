package com.merlita.diariolab.Modelos;

import java.util.Objects;

public class Estudio {
    private String nombre;          // Clave primaria
    private String descripcion;

    // Constructor
    public Estudio(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        Estudio person = (Estudio) o;
        // field comparison
        return Objects.equals(nombre, person.nombre);
    }
}