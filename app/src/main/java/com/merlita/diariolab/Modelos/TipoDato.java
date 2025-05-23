package com.merlita.diariolab.Modelos;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;

public class TipoDato implements Parcelable, Cloneable {

    private int idTipo=0;
    private String nombre;       // VARCHAR(20)
    private String tipoDato;     // VARCHAR(20)
    private String descripcion;  // VARCHAR(100)
    private String fkEstudio;    // NVARCHAR(50)

    //Variable auxiliar para medir las demás longitudes en el gráfico:
    private static int maximaLongitud = 0;

    // Constructor
    public TipoDato(String nombre, String tipoDato, String descripcion, String fkEstudio) {
        this.nombre = nombre;
        this.tipoDato = tipoDato;
        this.descripcion = descripcion;
        this.fkEstudio = fkEstudio;
    }
    // Con cualitativo
    public TipoDato(String nombre, String tipoDato, String descripcion,
                    String fkEstudio, ArrayList<Cualitativo> listaCualitativos) {
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

    /**
     * CONSTRUCTOR PARA BUSCAR
     * @param fkTipoDato
     * @param fkTipoEstudio
     */
    public TipoDato(String fkTipoDato, String fkTipoEstudio) {
        this.nombre = fkTipoDato;
        this.fkEstudio = fkTipoEstudio;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // Método para copia profunda:
    public static ArrayList<TipoDato> copiaPorValor(ArrayList<TipoDato> original) {
        ArrayList<TipoDato> copia = new ArrayList<>();
        for (TipoDato item : original) {
            try {
                copia.add((TipoDato) item.clone()); // Clona cada objeto
            } catch (CloneNotSupportedException e) {
                Log.d("MyAdapter", "Clonado no conseguido");
            }
        }
        return copia;
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


    public void setMaximaLongitud(int siguienteLongitud){
        if(siguienteLongitud > this.maximaLongitud){
            this.maximaLongitud = siguienteLongitud;
        }
    }
    public int getMaximaLongitud(){
        return maximaLongitud;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TipoDato tipoDato = (TipoDato) o;
        return Objects.equals(nombre, tipoDato.nombre) && Objects.equals(fkEstudio, tipoDato.fkEstudio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, fkEstudio);
    }
}

