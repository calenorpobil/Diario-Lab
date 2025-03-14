package com.merlita.diariolab.Modelos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TipoDato implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    // Método para crear un objeto TipoDato desde un Parcel
    protected TipoDato(Parcel in) {
        nombre = in.readString();
        tipoDato = in.readString();
        descripcion = in.readString();
        fkEstudio = in.readString();
    }

    // Creator para crear instancias de TipoDato desde un Parcel
    public static final Creator<TipoDato> CREATOR = new Creator<TipoDato>() {
        @Override
        public TipoDato createFromParcel(Parcel in) {
            return new TipoDato(in);
        }

        @Override
        public TipoDato[] newArray(int size) {
            return new TipoDato[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(nombre);
        parcel.writeString(tipoDato);
        parcel.writeString(descripcion);
        parcel.writeString(fkEstudio);
    }
}