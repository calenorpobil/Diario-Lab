package com.merlita.diariolab.Modelos;

import static com.merlita.diariolab.MainActivity.DB_VERSION;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.time.LocalDate;

public class Ocurrencia implements Parcelable {
    private final String id = "OC";
    private String cod;
    private LocalDate fecha;  // DATETIME
    private String fkEstudioN;   // VARCHAR(50)

    // Constructor
    public Ocurrencia(int num, LocalDate fecha, String fkEstudioN) {
        this.cod = id+num;
        this.fecha = fecha;
        this.fkEstudioN = fkEstudioN;
    }


    public Ocurrencia(LocalDate fecha, String fkEstudio, String id) {
        this.cod = id;
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
























