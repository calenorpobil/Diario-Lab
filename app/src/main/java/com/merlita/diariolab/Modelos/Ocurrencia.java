
package com.merlita.diariolab.Modelos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class Ocurrencia implements Parcelable {
    private final String prefix = "OC";
    private String cod;
    private LocalDate fecha;  // DATETIME
    private String fkEstudioN;   // VARCHAR(50)

    /**
     * CONSTRUCTOR CREAR CODIGO
     * @param num
     * @param fecha
     * @param fkEstudioN
     */
    public Ocurrencia(int num, LocalDate fecha, String fkEstudioN) {
        this.cod = prefix + num;
        this.fecha = fecha;
        this.fkEstudioN = fkEstudioN;
    }

    /**
     * CONSTRUCTOR CON CODIGO DADO
     * @param cod
     * @param fecha
     * @param fkEstudio
     */
    public Ocurrencia(String cod, LocalDate fecha, String fkEstudio) {
        this.cod = cod;
        this.fecha = fecha;
        this.fkEstudioN = fkEstudio;
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
        cod = in.readString();
        fecha = LocalDate.parse(in.readString());
        fkEstudioN = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(cod);
        dest.writeString(fecha.toString());
        dest.writeString(fkEstudioN);
    }
}
























