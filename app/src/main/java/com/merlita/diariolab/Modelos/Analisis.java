package com.merlita.diariolab.Modelos;

import android.database.sqlite.SQLiteDatabase;

import com.merlita.diariolab.MainActivity;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;

public class Analisis {

    private Estudio estudio1, estudio2;
    private TipoDato tipo1, tipo2;
    private ArrayList<Dato> datos1, datos2;
    private ArrayList<ArrayList<Integer>> resuldatos;

    public Analisis(Estudio estudio1, Estudio estudio2,
                    TipoDato tipo1, TipoDato tipo2) {
        this.estudio1 = estudio1;
        this.estudio2 = estudio2;
        this.tipo1 = tipo1;
        this.tipo2 = tipo2;
        this.datos1 = recuperarDatos(tipo1);
        this.datos2 = recuperarDatos(tipo2);
        this.resuldatos = calcularResultados();
    }

    private ArrayList<Dato> recuperarDatos(TipoDato tipo) {
        ArrayList<Dato> res = new ArrayList<>();

        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(null,
                            "DBEstudios", null,  MainActivity.DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getDatosDeTipo(db, tipo.getFkEstudio(), tipo.getNombre());

            db.close();
        }



        return res;
    }


    private ArrayList<ArrayList<Integer>> calcularResultados() {
        ArrayList<ArrayList<Integer>> res = new ArrayList<>();

        //MISMO ESTUDIO
        if(estudio1.getNombre().equals(estudio2.getNombre())){
            int size1 = datos1.size();
            int size2 = datos2.size();

            if(size2<size1){
                ArrayList<Dato> aux;
                aux = datos1;
                datos1 = datos2;
                datos2 = aux;
            }

            for (int i = 0; i < datos2.size(); i++) {

                String fecha = getFecha(datos2.get(0).getFkOcurrencia());




            }







            //DISTINTOS ESTUDIOS
        }else{





        }

        return res;
    }

    private String getFecha(String fkOcurrencia) {
        String res="";


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(null,
                            "DBEstudios", null,  MainActivity.DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            Ocurrencia aux = usdbh.getOcurrencia(db, estudio1.getNombre());

            db.close();
        }


        return res;
    }

    public Estudio getEstudio1() {
        return estudio1;
    }

    public void setEstudio1(Estudio estudio1) {
        this.estudio1 = estudio1;
    }

    public Estudio getEstudio2() {
        return estudio2;
    }

    public void setEstudio2(Estudio estudio2) {
        this.estudio2 = estudio2;
    }

    public TipoDato getTipo1() {
        return tipo1;
    }

    public void setTipo1(TipoDato tipo1) {
        this.tipo1 = tipo1;
    }

    public TipoDato getTipo2() {
        return tipo2;
    }

    public void setTipo2(TipoDato tipo2) {
        this.tipo2 = tipo2;
    }

    public ArrayList<Dato> getDatos1() {
        return datos1;
    }

    public void setDatos1(ArrayList<Dato> datos1) {
        this.datos1 = datos1;
    }

    public ArrayList<Dato> getDatos2() {
        return datos2;
    }

    public void setDatos2(ArrayList<Dato> datos2) {
        this.datos2 = datos2;
    }

    public ArrayList<ArrayList<Integer>> getResuldatos() {
        return resuldatos;
    }

    public void setResuldatos(ArrayList<ArrayList<Integer>> resuldatos) {
        this.resuldatos = resuldatos;
    }
}
