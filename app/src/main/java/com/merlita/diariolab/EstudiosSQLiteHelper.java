package com.merlita.diariolab;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;


public class EstudiosSQLiteHelper extends SQLiteOpenHelper {

    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE ESTUDIO (NOMBRE VARCHAR(50) PRIMARY KEY, " +
            "DESCRIPCION VARCHAR(9), EMOJI TEXT)";
    String sqlCreate1 = "CREATE TABLE OCURRENCIA( FECHA DATETIME PRIMARY KEY, FK_ESTUDIO_N VARCHAR(50), " +
            "CONSTRAINT  FK_OC_ES FOREIGN KEY (FK_ESTUDIO_N)  REFERENCES  ESTUDIO (NOMBRE));";
    String sqlCreate2 = "CREATE TABLE DATO (FK_TIPO_N VARCHAR(50), FK_TIPO_E VARCHAR(50), " +
            "FK_OCURRENCIA DATETIME, VALOR_TEXT TEXT, " +
            "CONSTRAINT FK_DA_TI FOREIGN KEY (FK_TIPO_N, FK_TIPO_E)  " +
            "REFERENCES DATO_TIPO (NOMBRE, FK_ESTUDIO), " +
            "CONSTRAINT FK_DA_OC FOREIGN KEY (FK_OCURRENCIA)  " +
            "REFERENCES OCURRENCIA (FECHA), " +
            "PRIMARY KEY  (VALOR_TEXT, FK_OCURRENCIA));";
    String sqlCreate3 = "CREATE TABLE DATO_TIPO (NOMBRE  VARCHAR(20), TIPO_DATO  VARCHAR(20), " +
            "DESCRIPCION  VARCHAR(100), FK_ESTUDIO NVARCHAR(50), " +
            "CONSTRAINT FK_TI_ES FOREIGN KEY (FK_ESTUDIO) " +
            "REFERENCES ESTUDIO(NOMBRE), CONSTRAINT " +
            "CHK_TIPO CHECK (TIPO_DATO IN ('Número', 'Texto', 'Fecha')), PRIMARY KEY  (NOMBRE));";

    public long insertarEstudio(SQLiteDatabase db, Estudio est){
        long newRowId=0;

        ContentValues values = new ContentValues();
        values.put("NOMBRE", est.getNombre());
        values.put("DESCRIPCION", est.getDescripcion());
        values.put("EMOJI", est.getEmoji());

        newRowId = db.insert("Estudio", null, values);

        return newRowId;
    }

    public long insertarDato(SQLiteDatabase db, Dato dato){
        long newRowId=0;

        ContentValues values = new ContentValues();
        values.put("FK_TIPO_E", dato.getFkTipoE());
        values.put("FK_TIPO_N", dato.getFkTipoN());
        values.put("FK_TIPO_N", dato.getFkTipoN());
        values.put("FK_OCURRENCIA", dato.getFkOcurrencia().toString());
        values.put("VALOR_TEXT", dato.getValorText());

        newRowId = db.insert("DATO", null, values);

        return newRowId;
    }

    public long insertarOcurrencia(SQLiteDatabase db, Ocurrencia ocurrencia) {
        long newRowId = 0;

        ContentValues values = new ContentValues();
        values.put("FECHA", ocurrencia.getFecha().toString()); // DATETIME se convierte a String
        values.put("FK_ESTUDIO_N", ocurrencia.getFkEstudioN());

        newRowId = db.insert("OCURRENCIA", null, values);

        return newRowId;
    }


    public long insertarTipoDato(SQLiteDatabase db, TipoDato datoTipo) {
        long newRowId = 0;

        ContentValues values = new ContentValues();
        values.put("NOMBRE", datoTipo.getNombre());
        values.put("TIPO_DATO", datoTipo.getTipoDato());
        values.put("DESCRIPCION", datoTipo.getDescripcion());
        values.put("FK_ESTUDIO", datoTipo.getFkEstudio());

        newRowId = db.insert("DATO_TIPO", null, values);

        return newRowId;
    }

    public long editarTipoDato(SQLiteDatabase db, TipoDato datoTipo, TipoDato nuevoDatoTipo) {
        long newRowId = 0;

        ContentValues values = new ContentValues();
        values.put("NOMBRE", nuevoDatoTipo.getNombre());
        values.put("TIPO_DATO", nuevoDatoTipo.getTipoDato());
        values.put("DESCRIPCION", nuevoDatoTipo.getDescripcion());
        values.put("FK_ESTUDIO", nuevoDatoTipo.getFkEstudio());

        newRowId = db.update("DATO_TIPO", values, "NOMBRE = ?", new String[]{datoTipo.getNombre()});

        return newRowId;
    }


    public long borrarSQL(Estudio libro){
        long res=-1;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("Estudio",
                "nombre=?", new String[]{libro.getNombre()});

        db.close();
        return res;
    }

    public long borrarEstudios() {
        long res=-1;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("ESTUDIO",
                null, null);

        db.close();
        return res;
    }

    public void borrarSQL(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS estudio");
        db.execSQL("DROP TABLE IF EXISTS OCURRENCIA");
        db.execSQL("DROP TABLE IF EXISTS DATO_TIPO");
        db.execSQL("DROP TABLE IF EXISTS DATO");
        onCreate(db);

        db.close();
    }

    public long borrarDatos() {
        long res=-1;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("DATO",
                null, null);

        db.close();
        return res;
    }


    public long borrarEstudio(Estudio actual, SQLiteDatabase db) {

        long res=-1;

        res = db.delete("ESTUDIO",
                "NOMBRE = ?", new String[]{actual.getNombre()});

        return res;
    }

    public long borrarTipo_Datos() {
        long res=-1;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("TIPO_DATO",
                null, null);

        db.close();
        return res;
    }
    public long borrarOcurrencias() {
        long res=-1;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("OCURRENCIA",
                null, null);

        db.close();
        return res;
    }
    public long editarSQL(SQLiteDatabase db, Estudio nuevo, int nuevaCuenta){
        long res=-1;
        ContentValues values = new ContentValues();
        values.put("NOMBRE", nuevo.getNombre());
        values.put("DESCRIPCION", nuevo.getDescripcion());
        values.put("CUENTA", nuevaCuenta);

        // Actualizar usando el ID como condición
        String[] id = {nuevo.getNombre()};
        res=db.update("Estudio",
                values,
                "nombre = ?",
                id);
        return res;
    }


    public EstudiosSQLiteHelper(Context contexto, String nombre,
                                CursorFactory factory, int version) {
        super(contexto, nombre, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creaci�n de la tabla
        db.execSQL(sqlCreate);
        db.execSQL(sqlCreate1);
        db.execSQL(sqlCreate2);
        db.execSQL(sqlCreate3);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior,
                          int versionNueva) {
        //NOTA: Por simplicidad del ejemplo aqu� utilizamos directamente
        //      la opci�n de eliminar la tabla anterior y crearla de nuevo
        //      vac�a con el nuevo formato.
        //      Sin embargo lo normal ser� que haya que migrar datos de la
        //      tabla antigua a la nueva, por lo que este m�todo deber�a
        //      ser m�s elaborado.

        //Se elimina la versi�n anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS estudio");
        db.execSQL("DROP TABLE IF EXISTS OCURRENCIA");
        db.execSQL("DROP TABLE IF EXISTS DATO_TIPO");
        db.execSQL("DROP TABLE IF EXISTS DATO");

        onCreate(db);

    }

}
