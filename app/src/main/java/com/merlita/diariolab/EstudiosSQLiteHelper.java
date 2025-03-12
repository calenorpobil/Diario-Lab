package com.merlita.diariolab;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.merlita.diariolab.Modelos.Estudio;


public class EstudiosSQLiteHelper extends SQLiteOpenHelper {

    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE IF NOT EXISTS ESTUDIO(" +
            "NOMBRE VARCHAR(50) PRIMARY KEY UNIQUE," +
            "DESCRIPCION VARCHAR(9), " +
            "CUENTA INTEGER);";

    public long insertarSQL(Estudio libro){
        SQLiteDatabase db = getWritableDatabase();
        long newRowId=0;

        ContentValues values = new ContentValues();
        values.put("NOMBRE", libro.getNombre());
        values.put("DESCRIPCION", libro.getDescripcion());
        values.put("CUENTA", libro.getCuenta());

        newRowId = db.insert("Estudio", null, values);

        db.close();
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

    public long borrarTodo() {
        long res=-1;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("Estudio",
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
        db.execSQL("DROP TABLE IF EXISTS estudio");

        db.execSQL(sqlCreate);
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
        db.execSQL("DROP TABLE IF EXISTS libros");

        //Se crea la nueva versi�n de la tabla
        db.execSQL(sqlCreate);
    }

}
