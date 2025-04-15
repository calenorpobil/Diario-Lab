package com.merlita.diariolab.Modelos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class Ocurrencia implements Parcelable {
    private final String id = "OC";
    private static int num=0;
    private String cod;
    private LocalDate fecha;  // DATETIME
    private String fkEstudioN;   // VARCHAR(50)

    // Constructor
    public Ocurrencia(LocalDate fecha, String fkEstudioN) {
        num++;
        this.cod = id+num;
        this.fecha = fecha;
        this.fkEstudioN = fkEstudioN;
    }

    public static final Creator<Ocurrencia> CREATOR = new Creator<Ocurrencia>() {
        @Override
        public Ocurrencia createFromParcel(Parcel in) {
            return new Ocurrencia(in);
        }

        @Override
        public Ocurrencia[] newArray(int size) {
            return new Ocurrencia[size];
        }
    };

    // Getters y Setters

    public String getCod() {return cod;}

    public void setCod(String cod) {this.cod = cod;}

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getFkEstudioN() {
        return fkEstudioN;
    }

    public void setFkEstudioN(String fkEstudioN) {
        this.fkEstudioN = fkEstudioN;
    }

    protected Ocurrencia(Parcel in){
        fecha = LocalDate.parse(in.readString());
        fkEstudioN = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(fecha.toString());
        dest.writeString(fkEstudioN);
    }
}
























