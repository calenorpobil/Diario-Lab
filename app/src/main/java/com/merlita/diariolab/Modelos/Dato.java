package com.merlita.diariolab.Modelos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Dato  implements Parcelable {

    private static int id_dato=0;            // INTEGER
    private String fkTipoDato;      // VARCHAR(50)
    private String fkTipoEstudio;      // VARCHAR(50)
    private String fkOcurrencia; // DATETIME
    private String valorText;    // TEXT

    // Constructor
    public Dato(String fkTipoN, String fkTipoE, String fkOcurrencia, String valorText) {
        this.id_dato = id_dato+1;
        this.fkTipoDato = fkTipoN;
        this.fkTipoEstudio = fkTipoE;
        this.fkOcurrencia = fkOcurrencia;
        this.valorText = valorText;
    }

    public Dato(String fkTipoN) {
        setFkTipoDato(fkTipoN);
    }



    // Getters y Setters
    public String getFkTipoDato() {
        return fkTipoDato;
    }

    public void setFkTipoDato(String fkTipoDato) {
        this.fkTipoDato = fkTipoDato;
    }

    public String getFkTipoEstudio() {
        return fkTipoEstudio;
    }

    public void setFkTipoEstudio(String fkTipoEstudio) {
        this.fkTipoEstudio = fkTipoEstudio;
    }

    public String getFkOcurrencia() {
        return fkOcurrencia;
    }

    public void setFkOcurrencia(String fkOcurrencia) {
        this.fkOcurrencia = fkOcurrencia;
    }

    public String getValorText() {
        return valorText;
    }

    public void setValorText(String valorText) {
        this.valorText = valorText;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    // Método para crear un objeto TipoDato desde un Parcel
    protected Dato(Parcel in) {
        id_dato = in.readInt();
        fkTipoDato = in.readString();
        fkTipoEstudio = in.readString();
        fkOcurrencia = in.readString();
        valorText = in.readString();
    }

    // Creator para crear instancias de TipoDato desde un Parcel
    public static final Creator<Dato> CREATOR = new Creator<Dato>() {
        @Override
        public Dato createFromParcel(Parcel in) {
            return new Dato(in);
        }

        @Override
        public Dato[] newArray(int size) {
            return new Dato[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id_dato);
        parcel.writeString(fkTipoDato);
        parcel.writeString(fkTipoEstudio);
        parcel.writeString(fkOcurrencia);
        parcel.writeString(valorText);

    }
}