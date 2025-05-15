package com.merlita.diariolab.Modelos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.merlita.diariolab.MainActivity;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Analisis {

    private Context context;
    private Estudio estudio1, estudio2;
    private TipoDato tipo1, tipo2;
    private ArrayList<Dato> datos1, datos2;
    private HashMap<Pareja<String, String>, Integer> resulDatos;
    private ArrayList<Pareja<String, String>> parejas = new ArrayList<>();
    private String[] tiposFilas, tiposColumnas;
    int repesMax=0, repesMin=0;

    public Analisis(Context context, String estudio1, String estudio2,
                    String tipo1, String tipo2) {
        this.context = context;
        this.estudio1 = recuperarEstudio(estudio1);
        this.estudio2 = recuperarEstudio(estudio2);
        this.tipo1 = recuperarTipo(estudio1, tipo1);
        this.tipo2 = recuperarTipo(estudio2, tipo2);
        this.datos1 = recuperarDatos(this.tipo1);
        this.datos2 = recuperarDatos(this.tipo2);
        this.resulDatos = calcularResultados();
    }

    private TipoDato recuperarTipo(String estudio, String nombreTipo) {
        TipoDato res = null;

        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(context,
                            "DBEstudios", null,  MainActivity.DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getTipoDato(db, estudio, nombreTipo);

            db.close();
        }

        return res;
    }

    private Estudio recuperarEstudio(String nombreEstudio) {
        Estudio res = null;

        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(context,
                            "DBEstudios", null,  MainActivity.DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getEstudio(db, nombreEstudio);

            db.close();
        }

        return res;
    }

    private ArrayList<Dato> recuperarDatos(TipoDato tipo) {
        ArrayList<Dato> res = new ArrayList<>();

        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(context,
                            "DBEstudios", null,  MainActivity.DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getDatosDeTipo(db, tipo.getFkEstudio(), tipo.getNombre());

            db.close();
        }
        return res;
    }


    private HashMap<Pareja<String, String>, Integer> calcularResultados() {
        HashMap<Pareja<String, String>, Integer> res = new HashMap<>();

        //MISMO ESTUDIO
        if(estudio1.getNombre().equals(estudio2.getNombre())){
            int size1 = datos1.size(); // Filas
            int size2 = datos2.size(); // Columnas

            if(size2<size1){
                ArrayList<Dato> aux;
                aux = datos1;
                datos1 = datos2;
                datos2 = aux;
            }

            // CADA OCURRENCIA:
            ArrayList<Pareja<String, String>> ocurrencias = new ArrayList<>();
            HashMap<Pareja<String, String>, Integer> ocurrenciasC = new HashMap<>();
            for (int i = 0; i < datos2.size(); i++) {
                Dato explorado = datos2.get(i);
                Ocurrencia ocurrencia = getOcurrenciaDeDato(explorado.getFkOcurrencia());
                // Busca el otro dato de la misma ocurrencia:
                int index = datos1.indexOf(new Dato(ocurrencia));
                Dato correspondiente = datos1.get(index);

                ocurrencias.add(new Pareja(explorado.getValorText(), correspondiente.getValorText()));
                // Colocar un 0 o la cuenta
                Pareja pareja = new Pareja(
                        explorado.getValorText(),
                        correspondiente.getValorText());
                int cuenta = ocurrenciasC.getOrDefault(pareja,0)+1;
                if (cuenta == 1) parejas.add(pareja);
                ocurrenciasC.put(
                        new Pareja(explorado.getValorText(), correspondiente.getValorText()),
                        cuenta);

                // MÁXIMAS Y MÍNIMAS REPETICIONES DE LA PAREJA
                if (repesMax == 0) {
                    repesMax = cuenta;
                    repesMin = cuenta;
                }else{
                    if(cuenta>repesMax) repesMax = cuenta;
                    if(cuenta>repesMin) repesMin = cuenta;
                }


            }
            res = ocurrenciasC;



        // ESTUDIOS DISTINTOS
        }else{

        }

        return res;
    }

    private Ocurrencia getOcurrenciaDeDato(String fkOcurrencia) {
        Ocurrencia res;


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(context,
                            "DBEstudios", null,  MainActivity.DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getOcurrenciaPorId(db, fkOcurrencia);

            db.close();
        }

        return res;
    }

    public ArrayList<ArrayList<String>> getListaTabla(){
        ArrayList<ArrayList<String>> listaTabla = new ArrayList<>();
        ArrayList<String> filaCabecera = new ArrayList<>();
        ArrayList<ArrayList<String>> filas = new ArrayList<>();

        filaCabecera.add("");

        for (int i = 0; i < datos2.size(); i++) { // Columnas
            filaCabecera.add(datos2.get(i).getValorText());
        }
        listaTabla.add(filaCabecera);
        Pareja<String, String> pareja;
        int numColumnas = filaCabecera.size();
        for (int i = 0; i < numColumnas; i++) {
            filas.get(i) = new ArrayList<>();
            ArrayList<String> filaActual = filas.get(i);
            // Columna 0:
            if(i==0){
                filaActual.add(datos1.get(i).getValorText());
            // Resto de columnas:
            }else{
                // Posicion obvia la primera fila:
                int posicion = i+numColumnas;
                int fila = posicion / numColumnas;
                int columna = posicion % numColumnas;
                pareja = new Pareja<>(
                        datos2.get(columna-1).getValorText(), datos1.get(fila-1).getValorText());
                String valorCelda = resulDatos.get(pareja)+"";
                filaActual.add(valorCelda);
            }
            listaTabla.add(filaActual);
        }

        return listaTabla;
    }

    public int getRepesMax() {
        return repesMax;
    }

    public void setRepesMax(int repesMax) {
        this.repesMax = repesMax;
    }

    public int getRepesMin() {
        return repesMin;
    }

    public void setRepesMin(int repesMin) {
        this.repesMin = repesMin;
    }

    public Estudio getEstudio1() {
        return estudio1;
    }

    public ArrayList<Pareja<String, String>> getParejas() {
        return parejas;
    }

    public void setParejas(ArrayList<Pareja<String, String>> parejas) {
        this.parejas = parejas;
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

    public HashMap<Pareja<String, String>, Integer> getResulDatos() {
        return resulDatos;
    }

    public void setResulDatos(HashMap<Pareja<String, String>, Integer> resulDatos) {
        this.resulDatos = resulDatos;
    }
}
