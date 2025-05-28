package com.merlita.diariolab.Modelos;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.merlita.diariolab.MainActivity;

import java.util.ArrayList;
import java.util.Objects;

public class Estudio implements Parcelable {
    private String nombre;          // Clave primaria
    private String descripcion;
    private String emoji;
    private int reps;

    // Constructor
    public Estudio(String nombre, String descripcion, String emoji, int reps) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.emoji = emoji;
        this.reps  = reps;
    }

    public Estudio(String nombre, String descripcion, String emoji) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.emoji = emoji;
        this.reps = 0;
    }

    // Getters y Setters
    public String getEmoji() {return emoji;}
    public void setEmoji(String emoji) {this.emoji = emoji;}

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

    public int getReps() {return reps;}

    public void setReps(int reps) {this.reps = reps;}

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

    // Método para crear un objeto TipoDato desde un Parcel
    protected Estudio(Parcel in) {
        nombre = in.readString();
        descripcion = in.readString();
        emoji = in.readString();
    }


    // Creator para crear instancias de TipoDato desde un Parcel
    public static final Creator<Estudio> CREATOR = new Creator<Estudio>() {
        @Override
        public Estudio createFromParcel(Parcel in) {
            return new Estudio(in);
        }

        @Override
        public Estudio[] newArray(int size) {
            return new Estudio[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(nombre);
        parcel.writeString(descripcion);
        parcel.writeString(emoji);
    }
}