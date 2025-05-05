package com.merlita.diariolab.Modelos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Cualitativo implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    protected Cualitativo(Parcel in) {
        this.titulo = in.readString();
        this.fk_dato_tipo_e = in.readString();
        this.fk_dato_tipo_t = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(titulo);
        dest.writeString(fk_dato_tipo_e);
        dest.writeString(fk_dato_tipo_t);
    }
    public static final Creator<Cualitativo> CREATOR = new Creator<Cualitativo>() {
        @Override
        public Cualitativo createFromParcel(Parcel in) {
            return new Cualitativo(in);
        }

        @Override
        public Cualitativo[] newArray(int size) {
            return new Cualitativo[size];
        }
    };

}
