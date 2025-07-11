package com.merlita.diariolab;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorDatos;
import com.merlita.diariolab.Adaptadores.AdaptadorEstudios;
import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.time.LocalDate;
import java.util.ArrayList;


public class OcurrenciaActivity extends AppCompatActivity
        implements AdaptadorDatos.OnButtonClickListener, AdaptadorDatos.DatePickerListener {
    private static final int DB_VERSION = MainActivity.DB_VERSION;

    TextView tvTitulo, tvFecha, tvReps;
    Button btGuardar;
    ArrayList<TipoDato> listaTiposDato = new ArrayList<>();
    ArrayList<Dato> listaDatos = new ArrayList<>();
    AdaptadorDatos adaptadorDatos;
    RecyclerView vistaRecycler;
    private String idOcurrencia;
    private LocalDate fechaOcurrencia;
    private int posicion;
    Estudio estudioActual;
    private String fk_estudio;
    private Boolean esNueva;
    private String emojiReps = "\uD83D\uDD01\uFE0F";
    private int reps = 1;


    private void toast(String e) {
        if(e!=null){
            Toast.makeText(this, e,
                    Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ocurrencia);

        Bundle upIntent = this.getIntent().getExtras();
        assert upIntent != null;

        try{
            idOcurrencia = upIntent.getString("FECHA_OCURRENCIA");
        } catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        fk_estudio = upIntent.getString("FK_ESTUDIO");
        esNueva = upIntent.getBoolean("ES_NUEVA");
        posicion = upIntent.getInt("INDEX");

        if(fk_estudio!=null){
            tvTitulo = findViewById(R.id.tvEstudioElegir);
            tvReps = findViewById(R.id.tvRepeticiones);
            tvFecha = findViewById(R.id.tvFecha);
            btGuardar = findViewById(R.id.btnGuardar);
            vistaRecycler = findViewById(R.id.rvOcurrencias);


            listaTiposDato = getTiposDato();

            for (int i = 0; i < listaTiposDato.size(); i++) {
                listaDatos.add(new Dato(
                        listaTiposDato.get(i),
                        fk_estudio,
                        idOcurrencia,
                        ""));
            }

            adaptadorDatos = new AdaptadorDatos(
                    this, listaDatos, this,
                    listaTiposDato, this);


            vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
            vistaRecycler.setAdapter(adaptadorDatos);


            //Averiguar el estudio actual:
            for(Estudio estudioAux : MainActivity.listaEstudios) {
                if(estudioAux.getNombre().equals(fk_estudio)) {
                    estudioActual = estudioAux;
                    break;
                }
            }

            reps = getOcurrencia(estudioActual.getNombre());
            String repeticiones = (reps+1)+" "+emojiReps;
            tvReps.setText(repeticiones);


            tvTitulo.setText(estudioActual.getNombre());
            LocalDate hoy = LocalDate.now();
            String hoyString = hoy.getDayOfMonth()+"/"+hoy.getMonthValue()+"/"+hoy.getYear();
            tvFecha.setText(hoyString);
            fechaOcurrencia = hoy;

            actualizarDatos();
        }else{
            //NO HA FUNCIONADO EL CODIGO
        }




    }

    @Override
    public void mostrarDatePicker(int position) {
        AdaptadorEstudios.FragmentoFecha datePicker = new AdaptadorEstudios.FragmentoFecha();
        datePicker.setListener((view, year, month, dayOfMonth) -> {
            // Actualizar datos según la posición
            adaptadorDatos.actualizarFecha(position, year, month, dayOfMonth);
        });
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }
    private int getOcurrencia(String estudios) {
        int res = -1;


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null,  DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getCuentaOcurrencias(db, estudios);

            db.close();
        }

        return res;
    }

    private ArrayList<TipoDato> getTiposDato() {
        ArrayList<TipoDato> tiposResultado=null;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                tiposResultado = usdbh.getTiposDato(db, fk_estudio);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        return tiposResultado;
    }

    private void actualizarLocal() {
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorDatos);
    }

    private void insertarInicial() {
        try (EstudiosSQLiteHelper usdbh =
                     new EstudiosSQLiteHelper(this,
                             "DBEstudios", null, DB_VERSION);) {


            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Iniciar una transacción para mejorar el rendimiento
            try {
                db.beginTransaction();
                // Insertar estudios
                TipoDato nuevo = new TipoDato();
                if(usdbh.insertarTipoDato(db, nuevo)!=-1){
                    listaTiposDato.add(nuevo);
                }


                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error en transacción: " + e.getMessage());
            } finally {
                // Finalizar la transacción

                if (db != null && db.inTransaction()) {
                    db.endTransaction();
                }
                db.close();
            }

        }
    }


    public void actualizarDatos() {
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                listaTiposDato.clear();

                rellenarLista(db);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorDatos);

    }


    private void rellenarLista(SQLiteDatabase db) {
        String tipo=null, nombre=null, descripcion=null;
        int id=-1;
        Cursor c = db.rawQuery("select * from dato_tipo " +
                "where FK_ESTUDIO = ?",
                new String[]{estudioActual.getNombre()});

        while (c.moveToNext()) {
            int index=0;
            index = c.getColumnIndex("NOMBRE");
            nombre = c.getString(index);
            index = c.getColumnIndex("TIPO_DATO");
            tipo = c.getString(index);
            index = c.getColumnIndex("DESCRIPCION");
            descripcion  = c.getString(index);
            index = c.getColumnIndex("ID");
            id  = c.getInt(index);

            listaTiposDato.add(new TipoDato(id, nombre, tipo, descripcion, fk_estudio));
        }
        c.close();
    }

    private int getCuentaTipos() {
        int  res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);) {

            SQLiteDatabase db = usdbh.getWritableDatabase();
            res = usdbh.getCuentaTipos(db);
        }
        return res;
    }


    public void clickGuardar(View v) {
        String error = "";//comprobaciones(tvTitulo, tvFecha, listaTiposDato, "alta");

        if (error.isEmpty()) {
            Intent i = new Intent();

            //Valido campos obligatorios (según esquema SQL)
            try {
                // Preparar todos los datos para enviar
                int numOcurrencia = getOcurrencia();
                Ocurrencia datosOcurrencia = new Ocurrencia(numOcurrencia,
                        fechaOcurrencia, fk_estudio);
                listaDatos.get(0).setFkOcurrencia(datosOcurrencia.getCod());

                i.putExtra("OCURRENCIA", datosOcurrencia);
                i.putExtra("ESTUDIO", estudioActual);
                i.putParcelableArrayListExtra("DATOS", listaDatos);

                setResult(RESULT_OK, i);
            } finally {
                finish();
            }

        }else{
            toast(error);
        }
    }

    private int getOcurrencia() {
        int res=-1;


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null,  DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res =usdbh.getCuentaOcurrencias(db, fk_estudio);
            res = usdbh.estaElIDOcurrenciaLibre(db, res, fk_estudio);

            db.close();
        }

        return res;
    }

    public static String comprobaciones(TextView tvTitulo,
                TextView etDescripcion, ArrayList<TipoDato> listaTiposDato, String alta) {
        String correcto = "";
/*


        if(alta.equals("alta")){
            for (int i = 0; i < MainActivity.listaEstudios.size(); i++) {
                if(tvTitulo.getText().toString().
                        equals(MainActivity.listaEstudios.get(i).getNombre())){
                    correcto = "Ese estudio ya existe. ";
                }
            }
        }
*/

        if (tvTitulo.getText().toString().isEmpty() ||
                etDescripcion.getText().toString().isEmpty()) {
            correcto = "Complete los campos obligatorios. ";
        }
        //Check por cada TipoDato
        for (int i = 0; i < listaTiposDato.size(); i++) {
            if (correcto.isEmpty()) {
                /*
                if (listaDatos.get(i).getNombre().isEmpty()) {
                    correcto="Tienes que poner un nombre al dato. ";
                    break;
                }*/
            }
        }
        return correcto;
    }



    @Override
    public void onButtonClick(int position) {

    }
}
