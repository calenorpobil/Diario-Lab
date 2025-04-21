package com.merlita.diariolab.Utils;

import static com.merlita.diariolab.MainActivity.DB_VERSION;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;

import java.time.LocalDate;
import java.util.ArrayList;


public class EstudiosSQLiteHelper extends SQLiteOpenHelper {

    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE ESTUDIO (NOMBRE VARCHAR(50) PRIMARY KEY, " +
            "DESCRIPCION VARCHAR(9), EMOJI TEXT, REPS INTEGER)";
    String sqlCreate1 = "CREATE TABLE OCURRENCIA(ID VARCHAR(4), FECHA DATETIME, FK_ESTUDIO_N VARCHAR(50), " +
            "CONSTRAINT  FK_OC_ES FOREIGN KEY (FK_ESTUDIO_N)  REFERENCES  ESTUDIO (NOMBRE), " +
            "PRIMARY KEY (ID, FK_ESTUDIO_N));";
    String sqlCreate2 = "CREATE TABLE DATO (FK_TIPO_N VARCHAR(50), FK_TIPO_E VARCHAR(50), " +
            "ID_DATO INTEGER, "+
            "FK_OCURRENCIA VARCHAR(4), VALOR_TEXT TEXT, " +
            "CONSTRAINT FK_DA_TI FOREIGN KEY (FK_TIPO_N, FK_TIPO_E)  " +
            "REFERENCES DATO_TIPO (NOMBRE, FK_ESTUDIO), " +
            "CONSTRAINT FK_ES FOREIGN KEY (FK_TIPO_E) REFERENCES ESTUDIO (NOMBRE), "+
            "CONSTRAINT FK_DA_OC FOREIGN KEY (FK_OCURRENCIA)  " +
            "REFERENCES OCURRENCIA (ID), " +
            "PRIMARY KEY  (ID_DATO));";
    String sqlCreate3 = "CREATE TABLE DATO_TIPO (NOMBRE VARCHAR(20), TIPO_DATO  VARCHAR(20), " +
            "DESCRIPCION  VARCHAR(100), FK_ESTUDIO NVARCHAR(50), " +
            "CONSTRAINT FK_TI_ES FOREIGN KEY (FK_ESTUDIO) " +
            "REFERENCES ESTUDIO(NOMBRE), CONSTRAINT " +
            "CHK_TIPO CHECK (TIPO_DATO IN ('Número', 'Texto', 'Fecha', 'Tipo')), " +
            "PRIMARY KEY (NOMBRE, FK_ESTUDIO));";

    public long insertarEstudio(SQLiteDatabase db, Estudio est){
        long newRowId=0;

        ContentValues values = new ContentValues();
        values.put("NOMBRE", est.getNombre());
        values.put("DESCRIPCION", est.getDescripcion());
        values.put("EMOJI", est.getEmoji());

        newRowId = db.insert("Estudio", null, values);

        return newRowId;
    }

    public String getNombreTipo(SQLiteDatabase db, String nombre, String fk_estudio){
        String resp = "";
        ArrayList<TipoDato> tipoDatoRes = new ArrayList<>();

        String sql = "SELECT descripcion FROM dato_tipo WHERE fk_estudio = ? AND nombre = ?";

        Cursor c = db.rawQuery(sql, new String[]{fk_estudio, nombre});

        while(c.moveToNext()){
            resp = c.getString(0);
        }
        c.close();

        return resp;
    }


    public long insertarDato(SQLiteDatabase db, Dato dato){
        long newRowId=0;

        ContentValues values = new ContentValues();
        values.put("FK_TIPO_E", dato.getFkTipoEstudio());
        values.put("FK_TIPO_N", dato.getFkTipoDato());
        values.put("FK_TIPO_N", dato.getFkTipoDato());
        values.put("FK_OCURRENCIA", dato.getFkOcurrencia().toString());
        values.put("VALOR_TEXT", dato.getValorText());

        newRowId = db.insert("DATO", null, values);

        return newRowId;
    }

    public int getOcurrencia(SQLiteDatabase db, String fkEstudioN) {
        int res=-1;

        Cursor mCount= db.rawQuery("select count(*) from ocurrencia where fk_estudio_n = ?",
                new String[]{fkEstudioN});
        mCount.moveToFirst();
        res = mCount.getInt(0);
        mCount.close();

        return res;
    }

    public boolean estudioExiste(SQLiteDatabase db, String estudio){
        boolean res=false;

        String sql = "SELECT nombre FROM estudio WHERE nombre = ?;";
        Cursor c = db.rawQuery(sql, new String[]{estudio});

        while (c.moveToNext()){
            String nombre =
                    c.getString(0);
            if (nombre!=null) res=true;
        }
        c.close();

        return res;
    }

    public long insertarOcurrencia(SQLiteDatabase db, Ocurrencia ocurrencia) throws SQLiteException{
        long newRowId = 0;

        ContentValues values = new ContentValues();
        values.put("ID", ocurrencia.getCod());
        values.put("FECHA", ocurrencia.getFecha().toString()); // DATETIME se convierte a String
        values.put("FK_ESTUDIO_N", ocurrencia.getFkEstudioN());

        try{
            newRowId = db.insertOrThrow("OCURRENCIA", null, values);
        } catch (SQLiteException ex){
            Log.e("DB_ERROR", "Error en transacción: " + ex.getMessage());
        }

        return newRowId;
    }


    public long insertarTipoDato(SQLiteDatabase db, TipoDato datoTipo)  {
        long newRowId = 0;

        ContentValues values = new ContentValues();
        values.put("NOMBRE", datoTipo.getNombre());
        values.put("TIPO_DATO", datoTipo.getTipoDato());
        values.put("DESCRIPCION", datoTipo.getDescripcion());
        values.put("FK_ESTUDIO", datoTipo.getFkEstudio());

        newRowId = db.insert("DATO_TIPO", null, values);

        return newRowId;
    }


    public ArrayList<Dato> getDatos(SQLiteDatabase db, String fkOcurrencia, String nomEstudio) {

        ArrayList<Dato> datos = new ArrayList<>();

        String sql = "SELECT fk_tipo_n, fk_tipo_e, fk_ocurrencia, " +
                "valor_text FROM dato WHERE fk_ocurrencia = ? AND FK_TIPO_E = ?;";
        Cursor c = db.rawQuery(sql, new String[]{fkOcurrencia, nomEstudio});

        while (c.moveToNext()){
            datos.add(new Dato(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)));
        }
        c.close();

        return datos;
    }
    public ArrayList<TipoDato> getTiposDato(SQLiteDatabase db, String fk_estudio){
        long suc = 0;
        ArrayList<TipoDato> tipoDatoRes = new ArrayList<>();

        String sql = "SELECT nombre, tipo_dato, descripcion FROM dato_tipo WHERE fk_estudio = ?";

        Cursor c = db.rawQuery(sql, new String[]{fk_estudio});

        while(c.moveToNext()){
            tipoDatoRes.add(new TipoDato(c.getString(0), c.getString(1), c.getString(2)));
        }
        c.close();

        return tipoDatoRes;
    }

    public long editarTipoDato(SQLiteDatabase db, TipoDato datoTipo, TipoDato nuevoDatoTipo) {
        long newRowId = 0;

        ContentValues values = new ContentValues();
        values.put("NOMBRE", nuevoDatoTipo.getNombre());
        values.put("TIPO_DATO", nuevoDatoTipo.getTipoDato());
        values.put("DESCRIPCION", nuevoDatoTipo.getDescripcion());
        values.put("FK_ESTUDIO", nuevoDatoTipo.getFkEstudio());

        newRowId = db.update("DATO_TIPO", values,
                "NOMBRE = ?",
                new String[]{datoTipo.getNombre()});

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

        res = db.delete("DATO_TIPO",
                null, null);

        db.close();
        return res;
    }
    public long borrarTiposDatos_PorFK(SQLiteDatabase db, String fkEstudio) {
        long res=-1;

        res = db.delete("DATO_TIPO",
                "FK_ESTUDIO = ?",
                new String[]{fkEstudio});

        return res;
    }
    public long borrarDatos_PorFK(SQLiteDatabase db, String fkEstudio) {
        long res=-1;

        res = db.delete("DATO",
                "FK_TIPO_E = ?",
                new String[]{fkEstudio});

        return res;
    }
    public long borrarOcurrencia_PorFK(SQLiteDatabase db, String fkEstudio) {
        long res=-1;

        res = db.delete("OCURRENCIA",
                "FK_ESTUDIO_N = ?",
                new String[]{fkEstudio});

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
