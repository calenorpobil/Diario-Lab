package com.merlita.diariolab.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.merlita.diariolab.Modelos.Cualitativo;
import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;

public class EstudiosSQLiteHelper extends SQLiteOpenHelper {

    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE ESTUDIO (NOMBRE VARCHAR(50) PRIMARY KEY, " +
            "DESCRIPCION VARCHAR(9), EMOJI TEXT, REPS INTEGER)";
    String sqlCreate1 = "CREATE TABLE OCURRENCIA(ID VARCHAR(4), FECHA DATETIME, FK_ESTUDIO_N VARCHAR(50), " +
            "CONSTRAINT  FK_OC_ES FOREIGN KEY (FK_ESTUDIO_N)  REFERENCES  ESTUDIO (NOMBRE), " +
            "PRIMARY KEY (ID, FK_ESTUDIO_N));";
    String sqlCreate2 = "CREATE TABLE DATO (FK_TIPO_N INTEGER, FK_TIPO_E VARCHAR(50), " +
            "ID_DATO INTEGER, "+
            "FK_OCURRENCIA VARCHAR(4), VALOR_TEXT TEXT, " +
            "CONSTRAINT FK_DA_TI FOREIGN KEY (FK_TIPO_N)  " +
            "REFERENCES DATO_TIPO (ID), " +
            "CONSTRAINT FK_ES FOREIGN KEY (FK_TIPO_E) REFERENCES ESTUDIO (NOMBRE), "+
            "CONSTRAINT FK_DA_OC FOREIGN KEY (FK_OCURRENCIA)  " +
            "REFERENCES OCURRENCIA (ID), " +
            "PRIMARY KEY  (ID_DATO));";
    String sqlCreate3 = "CREATE TABLE DATO_TIPO (ID INTEGER, NOMBRE VARCHAR(20), TIPO_DATO  VARCHAR(20), " +
            "DESCRIPCION  VARCHAR(100), FK_ESTUDIO NVARCHAR(50), " +
            "FK_MAXLONG INTEGER, " +
            "CONSTRAINT FK_MAX FOREIGN KEY (FK_MAXLONG) " +
            "REFERENCES MAXLONG_TIPO(ID), "+
            "CONSTRAINT FK_TI_ES FOREIGN KEY (FK_ESTUDIO) " +
            "REFERENCES ESTUDIO(NOMBRE), CONSTRAINT " +
            "CHK_TIPO CHECK (TIPO_DATO IN ('Número', 'Texto', 'Fecha', 'Tipo')), " +
            "PRIMARY KEY (ID));";
    String sqlCreate4 = "CREATE TABLE CUALITATIVO (TITULO VARCHAR(20), FK_TIPO_DATO_T  VARCHAR(20), " +
            "FK_TIPO_DATO_E  VARCHAR(50), " +
            "CONSTRAINT FK_TIPO FOREIGN KEY (FK_TIPO_DATO_T, FK_TIPO_DATO_E) " +
            "REFERENCES DATO_TIPO (NOMBRE, FK_ESTUDIO),  "+
            "PRIMARY KEY (TITULO, FK_TIPO_DATO_T, FK_TIPO_DATO_E));";
    String sqlCreate5 = "CREATE TABLE DATOS_PRUEBA (ACTIVOS INTEGER);";

    private static int idMax = 0;


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
        values.put("FK_OCURRENCIA", dato.getFkOcurrencia().toString());
        values.put("VALOR_TEXT", dato.getValorText());

        newRowId = db.insert("DATO", null, values);

        return newRowId;
    }

    public int getCuentaOcurrencias(SQLiteDatabase db, String fkEstudioN) {
        int res=-1;

        Cursor mCount= db.rawQuery("select count(*) from ocurrencia where fk_estudio_n = ?",
                new String[]{fkEstudioN});
        mCount.moveToFirst();
        res = mCount.getInt(0);
        mCount.close();

        return res;
    }
    public int getCuentaTipos(SQLiteDatabase db) {
        int res=-1;

        Cursor mCount= db.rawQuery("select count(*) from dato_tipo",
                new String[]{});
        mCount.moveToFirst();
        res = mCount.getInt(0);
        mCount.close();

        return res;
    }
    public int estaElIDTipoLibre(SQLiteDatabase db, int id, ArrayList<Integer> pillados) {
        int res=-1;

        Cursor cID= db.rawQuery("select ID from dato_tipo " +
                        "where ID = ?;",
                new String[]{id+""});
        if(cID.moveToFirst()){
            id = estaElIDTipoLibre(db, (id+1), pillados);
        } else {
            if (!pillados.contains(id)){
                return id;
            }else{
                id = estaElIDTipoLibre(db, (id+1), pillados);
            }
        }
        cID.close();

        return id;
    }
    public int estaElIDOcurrenciaLibre(SQLiteDatabase db, int id,
                                       String fkEstudio) {
        int res=-1;

        Cursor cID= db.rawQuery("select ID from ocurrencia " +
                        "where ID = ? and FK_ESTUDIO_N = ?;",
                new String[]{"OC"+id, fkEstudio});
        if(cID.moveToFirst()){
            id = estaElIDOcurrenciaLibre(db, (id+1), fkEstudio);
        }else{
            return id;
        }
        cID.close();

        return id;
    }
    public int getCuentaTiposEstudio(SQLiteDatabase db, String fkEstudioN) {
        int res=-1;

        Cursor mCount= db.rawQuery("select count(*) from dato_tipo where fk_estudio = ?",
                new String[]{fkEstudioN});
        mCount.moveToFirst();
        res = mCount.getInt(0);
        mCount.close();

        return res;
    }
    public ArrayList<String> getListaNombreEstudios(SQLiteDatabase db) {
        ArrayList<String> nombres = new ArrayList<>();
        Cursor c= db.rawQuery("select nombre from estudio",
                null);
        c.moveToFirst();
        int cuenta = 0;
        while(c.moveToNext()){
            nombres.add(c.getString(0));
        }

        return nombres;
    }
    public Estudio getEstudio(SQLiteDatabase db, String nombreEstudio) {
        Estudio res=null;

        Cursor c= db.rawQuery("select * from estudio where nombre = ?",
                new String[]{nombreEstudio});
        c.moveToFirst();
        res = new Estudio(
                c.getString(0),
                c.getString(1),
                c.getString(2),
                c.getInt(3)
        );
        c.close();

        return res;
    }
    public Ocurrencia getOcurrencia(SQLiteDatabase db, String fkEstudioN) {
        Ocurrencia res=null;

        Cursor c= db.rawQuery("select * from ocurrencia where fk_estudio_n = ?",
                new String[]{fkEstudioN});
        c.moveToFirst();
        res = new Ocurrencia(
                c.getString(0),
                LocalDate.parse(c.getString(0)),
                c.getString(0)
        );
        c.close();

        return res;
    }
    public Ocurrencia getOcurrenciaPorId(SQLiteDatabase db, String id) {
        Ocurrencia res=null;

        Cursor c= db.rawQuery("select * from ocurrencia where id = ?",
                new String[]{id});
        c.moveToFirst();
        res = new Ocurrencia(
                c.getString(0),
                LocalDate.parse(c.getString(1)),
                c.getString(2)
        );
        c.close();

        return res;
    }
    public Ocurrencia getOcurrenciaPorIdYEstudio(SQLiteDatabase db, Dato dato) {
        Ocurrencia res=null;

        Cursor c= db.rawQuery("select * from ocurrencia where id = ? and fk_estudio_n = ?",
                new String[]{dato.getFkOcurrencia(), dato.getFkTipoEstudio()});
        c.moveToFirst();
        res = new Ocurrencia(
                c.getString(0),
                LocalDate.parse(c.getString(1)),
                c.getString(2)
        );
        c.close();

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
        values.put("FECHA", ocurrencia.getFecha().toString());  // DATETIME se convierte a String
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

//        insertarMaxLong(db,
//                datoTipo.getMaximaLongitud());

        ContentValues values = new ContentValues();
        values.put("ID", datoTipo.getId());
        values.put("NOMBRE", datoTipo.getNombre());
        values.put("TIPO_DATO", datoTipo.getTipoDato());
        values.put("DESCRIPCION", datoTipo.getDescripcion());
        values.put("FK_ESTUDIO", datoTipo.getFkEstudio());

        try{
            newRowId = db.insertOrThrow("DATO_TIPO", null, values);
        }catch(SQLiteException ex){
            String msg = ex.getMessage();
            System.out.println(msg);
        }

        return newRowId;
    }
    public long insertarTipoDatoEditAc(SQLiteDatabase db, TipoDato datoTipo)  {
        long newRowId = 0;

//        insertarMaxLong(db,
//                datoTipo.getMaximaLongitud());

        ContentValues values = new ContentValues();
        values.put("ID", datoTipo.getId());
        values.put("NOMBRE", datoTipo.getNombre());
        values.put("TIPO_DATO", datoTipo.getTipoDato());
        values.put("DESCRIPCION", datoTipo.getDescripcion());
        values.put("FK_ESTUDIO", datoTipo.getFkEstudio());

        try{
            newRowId = db.insertOrThrow("DATO_TIPO", null, values);
        }catch(SQLiteException ex){
            String msg = ex.getMessage();
            System.out.println(msg);
        }

        return newRowId;
    }
    public long insertarCualitativo(SQLiteDatabase db, Cualitativo cualitativo)  {
        long newRowId = 0;

        if(!cualitativo.getTitulo().isBlank()){
            ContentValues values = new ContentValues();
            values.put("TITULO", cualitativo.getTitulo());
            values.put("FK_TIPO_DATO_T", cualitativo.getFk_dato_tipo_t());
            values.put("FK_TIPO_DATO_E", cualitativo.getFk_dato_tipo_e());

            try{
                newRowId = db.insertOrThrow("CUALITATIVO", null, values);
            } catch (SQLiteException ex){
                String a = ex.getMessage();
                System.out.println(a);
            }
        }


        return newRowId;
    }



    public long insertarCualitativos(SQLiteDatabase db, ArrayList<Cualitativo> cualitativo)  {
        long newRowId = 0;



        for (int i = 0; i < cualitativo.size(); i++) {
            newRowId = insertarCualitativo(db, cualitativo.get(i));
        }

        return newRowId;
    }


    public long insertarMaxLong(SQLiteDatabase db,
                                int maxlong)  {
        long newRowId = 0;

        ContentValues values = new ContentValues();
        values.put("ID", ++idMax);
        values.put("LONG", maxlong);

        newRowId = db.insert("MAXLONG_TIPO", null, values);

        return newRowId;
    }



    public ArrayList<Dato> getDatosPorOcurrencia(SQLiteDatabase db,
                                                 String fkOcurrencia, String nomEstudio) {

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

    public ArrayList<Cualitativo> getCualitativos(
            SQLiteDatabase db, String fk_estudio, String fk_tipo) {

        ArrayList<Cualitativo> datos = new ArrayList<>();

        String sql = "SELECT titulo, fk_tipo_dato_e, fk_tipo_dato_t " +
                "FROM cualitativo WHERE fk_tipo_dato_t = ? AND fk_tipo_dato_e = ?;";
        Cursor c = db.rawQuery(sql, new String[]{fk_tipo, fk_estudio});

        while (c.moveToNext()){
            datos.add(new Cualitativo(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)));
        }
        c.close();

        return datos;
    }


    public ArrayList<Dato> getDatos(SQLiteDatabase db, String nomEstudio) {

        ArrayList<Dato> datos = new ArrayList<>();

        String sql = "SELECT fk_tipo_n, fk_tipo_e, fk_ocurrencia, " +
                "valor_text FROM dato WHERE FK_TIPO_E = ?;";
        Cursor c = db.rawQuery(sql, new String[]{nomEstudio});

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

    public ArrayList<Dato> getDatosUnicosDeTipo(SQLiteDatabase db,
                                                String nomEstudio, String nomTipo) {

        ArrayList<Dato> datos = new ArrayList<>();

        String sql = "SELECT fk_tipo_n, fk_tipo_e, fk_ocurrencia, " +
                "valor_text FROM dato WHERE FK_TIPO_E = ? AND fk_tipo_n = ?;";
        Cursor c = db.rawQuery(sql, new String[]{nomEstudio, nomTipo});

        while (c.moveToNext()){
            Dato dato = new Dato(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3));
            if(!containsDatoPorValor(datos, dato)){
                datos.add(dato);
            }
        }
        c.close();

        return datos;
    }

    private boolean containsDatoPorValor(ArrayList<Dato> datos, Dato dato) {
        for (Dato d: datos) {
            if(d.getValorText().equals(dato.getValorText())) return true;
        }
        return false;
    }

    public ArrayList<Dato> getDatosDeTipo(SQLiteDatabase db, String nomEstudio, String nomTipo) {

        ArrayList<Dato> datos = new ArrayList<>();

        String sql = "SELECT fk_tipo_n, fk_tipo_e, fk_ocurrencia, " +
                "valor_text FROM dato WHERE FK_TIPO_E = ? AND fk_tipo_n = ?;";
        Cursor c = db.rawQuery(sql, new String[]{nomEstudio, nomTipo});

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
    public ArrayList<Dato> getDatosDeTipoPorId(SQLiteDatabase db, int idTipo) {

        ArrayList<Dato> datos = new ArrayList<>();

        String sql = "SELECT fk_tipo_n, fk_tipo_e, fk_ocurrencia, " +
                "valor_text FROM dato WHERE fk_tipo_n = ?;";
        Cursor c = db.rawQuery(sql, new String[]{idTipo+""});

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


    public TipoDato getTipoDato(SQLiteDatabase db,
                                           String fkEstudio,
                                           String nombreTipo){
        long suc = 0;
        TipoDato tipoDatoRes = null;

        String sql = "SELECT id, nombre, tipo_dato, descripcion FROM dato_tipo WHERE " +
                "fk_estudio = ? AND nombre = ?";

        Cursor c = db.rawQuery(sql, new String[]{fkEstudio, nombreTipo});

        while(c.moveToNext()){
            tipoDatoRes = new TipoDato(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    fkEstudio);
        }
        c.close();

        return tipoDatoRes;
    }
    public ArrayList<TipoDato> getTiposDato(SQLiteDatabase db, String fk_estudio){
        long suc = 0;
        ArrayList<TipoDato> tipoDatoRes = new ArrayList<>();

        String sql = "SELECT id, nombre, tipo_dato, descripcion FROM dato_tipo WHERE fk_estudio = ?";

        Cursor c = db.rawQuery(sql, new String[]{fk_estudio});

        while(c.moveToNext()){
            tipoDatoRes.add(new TipoDato(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    fk_estudio));
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
    public int editarTiposDeLosDatos(SQLiteDatabase db,
                                     String viejoNombre, String nuevoNombre) {
        int res = -1;

        ContentValues values = new ContentValues();
        values.put("FK_TIPO_N", nuevoNombre);

        res = db.update("DATO", values,
                "FK_TIPO_N = ?",
                new String[]{viejoNombre});

        return res;
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
        db.execSQL("DROP TABLE IF EXISTS CUALITATIVO");
        db.execSQL("DROP TABLE IF EXISTS DATOS_PRUEBA");

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

    public void borrarTipoDato(TipoDato tipo) {
        int res = 0;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("DATO_TIPO",
                "NOMBRE = ? AND FK_ESTUDIO = ?",
                new String[]{tipo.getNombre(), tipo.getFkEstudio()});

        db.close();
    }

    public void borrarDatos_porTipo(TipoDato tipo) {
        int res = 0;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("DATO",
                "FK_TIPO_N = ? and FK_TIPO_E = ?",
                new String[]{tipo.getId()+"", tipo.getFkEstudio()});

        db.close();
    }
    public void editarDato_porTipoYDatoCualitativo(
            SQLiteDatabase db, Cualitativo cual,
            String estudio) {
        int res = 0;

        ContentValues values = new ContentValues();
        values.put("VALOR_TEXT", cual.getTitulo());

        // Actualizar usando el ID como condición
        String[] id = {cual.getFk_dato_tipo_e(), estudio};

        res = db.update("dato",
                        values,
                        "fk_tipo_n = ?, fk_tipo_e = ?",
                        id);

        db.close();
    }

    public void borrarOcurrenciasVacias(TipoDato tipo) {
        long res=-1;

        SQLiteDatabase db = getWritableDatabase();

        if(getCuentaTiposEstudio(db, tipo.getFkEstudio())==1){
            String query = "DELETE FROM ocurrencia " +
                    "WHERE fk_estudio_n = '"+tipo.getFkEstudio()+"'";
//                    "DELETE FROM ocurrencia WHERE ID = (select FK_OCURRENCIA from DATO where " +
//                    "FK_TIPO_N = '"+tipo.getNombre()+"' and FK_TIPO_E = '"+tipo.getFkEstudio()+"')" +
//                    " and FK_ESTUDIO_N = '"+tipo.getFkEstudio()+"';";
            db.execSQL(query);
        }




    }

    public long borrarTiposDatos_PorFK(SQLiteDatabase db, String fkEstudio) {
        long res=-1;

        res = db.delete("DATO_TIPO",
                "FK_ESTUDIO = ?",
                new String[]{fkEstudio});

        return res;
    }
    public long borrarCualitativos_PorFK(SQLiteDatabase db, String fkEstudio) {
        long res=-1;

        res = db.delete("CUALITATIVO",
                "FK_TIPO_DATO_E = ?",
                new String[]{fkEstudio});

        return res;
    }
    public void borrarCualitativos_PorEstudioYTipo(SQLiteDatabase db, String fkEstudio,
                                                   String fkTipo) {
        long res=-1;

        res = db.delete("CUALITATIVO",
                "FK_TIPO_DATO_E = ? AND FK_TIPO_DATO_T = ?",
                new String[]{fkEstudio, fkTipo});

    }
    public long borrarDatos_PorFK(SQLiteDatabase db, String fkEstudio) {
        long res=-1;

        res = db.delete("DATO",
                "FK_TIPO_E = ?",
                new String[]{fkEstudio});

        return res;
    }
    public long borrarDatos_PorOcurrencia(SQLiteDatabase db, Ocurrencia ocu) {
        long res=-1;

        res = db.delete("DATO",
                "FK_OCURRENCIA = ? and FK_TIPO_E = ?",
                new String[]{ocu.getCod(), ocu.getFkEstudioN()});

        return res;
    }
    public long borrarOcurrencia_PorFK(SQLiteDatabase db, String fkEstudio) {
        long res=-1;

        res = db.delete("OCURRENCIA",
                "FK_ESTUDIO_N = ?",
                new String[]{fkEstudio});

        borrarDatos_PorFK(db, fkEstudio);

        return res;
    }


    public int borrarocurrencia_PorID(SQLiteDatabase db, Ocurrencia ocurrencia) {
        int res=-1;

        res = db.delete("OCURRENCIA",
                "id = ? AND fk_estudio_n = ?",
                new String[]{ocurrencia.getCod(), ocurrencia.getFkEstudioN()});


        borrarDatos_PorOcurrencia(db, ocurrencia);

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
    public long borrarCualitativos() {
        long res=-1;
        SQLiteDatabase db = getWritableDatabase();

        res = db.delete("CUALITATIVO",
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
        db.execSQL(sqlCreate4);
        db.execSQL(sqlCreate5);
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
