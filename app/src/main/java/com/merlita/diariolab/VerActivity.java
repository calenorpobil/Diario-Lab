package com.merlita.diariolab;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorDatosVer;
import com.merlita.diariolab.Adaptadores.AdaptadorEstudios;
import com.merlita.diariolab.Adaptadores.AdaptadorOcurrencias;
import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.time.LocalDate;
import java.util.ArrayList;


public class VerActivity extends AppCompatActivity
        implements AdaptadorDatosVer.OnButtonClickListener, AdaptadorDatosVer.DatePickerListener,
        AdaptadorOcurrencias.OnButtonClickListener {
    private static final int DB_VERSION = MainActivity.DB_VERSION;

    TextView tvTitulo;
    Button btGuardar;
    ArrayList<Ocurrencia> listaOcurrencias = new ArrayList<>();
    ArrayList<Dato> listaDatos = new ArrayList<>();
    ArrayList<TipoDato> listaTipos = new ArrayList<>();
    AdaptadorOcurrencias adaptadorOcurrencias;
    AdaptadorDatosVer adaptadorDatos;
    RecyclerView rvOcurrencias, rvDatos;
    private LocalDate fechaOcurrencia;
    private int posicion;
    Estudio estudioOcurrencia;
    private String fk_estudio;
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
        setContentView(R.layout.activity_ver);

        Bundle upIntent = this.getIntent().getExtras();
        assert upIntent != null;

        estudioOcurrencia = upIntent.getParcelable("ESTUDIO");
        fk_estudio = estudioOcurrencia.getNombre();

        if(fk_estudio!=null){
            tvTitulo = findViewById(R.id.tvTitulo);
            btGuardar = findViewById(R.id.btnGuardar);
            rvOcurrencias = findViewById(R.id.rvOcurrencias);
            rvDatos = findViewById(R.id.rvDatos);

            rvOcurrencias.setLayoutManager(new LinearLayoutManager(this));
            rvOcurrencias.setAdapter(adaptadorOcurrencias);

            tvTitulo.setText(estudioOcurrencia.getNombre());

            listaTipos = getTiposDato();
            for (int i = 0; i < listaTipos.size(); i++) {
                listaDatos.add(new Dato(
                        listaTipos.get(i).getTipoDato(),
                        fk_estudio,
                        "",
                        ""));
            }



            adaptadorOcurrencias = new AdaptadorOcurrencias(
                    this, estudioOcurrencia, listaOcurrencias, this);
            adaptadorDatos = new AdaptadorDatosVer(
                    this, listaDatos, this, listaTipos, this);

            //Invertir el orden de las Ocurrencias:
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            rvOcurrencias.setLayoutManager(linearLayoutManager);
            rvOcurrencias.setLayoutManager(new LinearLayoutManager(this));
            rvOcurrencias.setAdapter(adaptadorOcurrencias);

            actualizarRvDatos();

        }else{
            //NO HA FUNCIONADO EL CODIGO
        }

        actualizarListas();


/*
        btNuevoTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaDatos.add(new Dato());
                actualizarLocal();
            }
        });*/


    }

    private void actualizarRvDatos() {
        rvDatos.setLayoutManager(new LinearLayoutManager(this));
        rvDatos.setAdapter(adaptadorDatos);
    }

    @Override
    public void mostrarDatePicker(int position) {
        AdaptadorEstudios.FragmentoFecha datePicker = new AdaptadorEstudios.FragmentoFecha();
        datePicker.setListener((view, year, month, dayOfMonth) -> {
            // Actualizar datos según la posición
            adaptadorOcurrencias.actualizarFecha(position, year, month, dayOfMonth);
        });
        datePicker.show(getSupportFragmentManager(), "datePicker");
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
        rvOcurrencias.setLayoutManager(new LinearLayoutManager(this));
        rvOcurrencias.setAdapter(adaptadorOcurrencias);
    }


    public void actualizarListas() {
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                listaOcurrencias.clear();

                rellenarLista(db);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        rvOcurrencias.setLayoutManager(new LinearLayoutManager(this));
        rvOcurrencias.setAdapter(adaptadorOcurrencias);

    }



    private ArrayList<Dato> getDatos(String codOcurrencia, String nomEstudio) {
        ArrayList<Dato> tiposResultado=null;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                tiposResultado = usdbh.getDatos(db, codOcurrencia, nomEstudio);

                db.close();
            } catch (Exception ex){
                toast("Juan");
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        return tiposResultado;
    }


    private void verDatosOcurrencia(String codOcurrencia, String nomEstudio) {
        listaDatos = getDatos(codOcurrencia, nomEstudio);
        listaTipos = getTiposDato();

        adaptadorDatos = new AdaptadorDatosVer(
                this, listaDatos, this, listaTipos, this);

        rvDatos.setAdapter(adaptadorDatos);

        //actualizarListas();
        actualizarRvDatos();
    }
    private void rellenarLista(SQLiteDatabase db) {
        LocalDate fecha=null;


        Cursor c = db.rawQuery("select * from ocurrencia " +
                "where FK_ESTUDIO_N = ?",
                new String[]{estudioOcurrencia.getNombre()});

        while (c.moveToNext()) {
            int index=0;
            index = c.getColumnIndex("FECHA");
            String par = c.getString(index);
            fecha = LocalDate.parse(par);
            index = c.getColumnIndex("ID");
            String id = c.getString(index);

            Ocurrencia ver = new Ocurrencia(fecha, fk_estudio);
            ver.setCod(id);

            listaOcurrencias.add(ver);



            tvTitulo.setText(fecha.toString()+" "+fk_estudio);

        }

        c.close();
    }


    public void clickGuardar(View v) {
        String error = "";//comprobaciones(tvTitulo, tvFecha, listaTiposDato, "alta");

        if (error.isEmpty()) {
            Intent i = new Intent();

            //Valido campos obligatorios (según esquema SQL)
            try {
                // Preparar todos los datos para enviar
                ArrayList<String> datosEstudio = new ArrayList<>();
                datosEstudio.add(tvTitulo.getText().toString());
                //datosEstudio.add(tvFecha.getText().toString());

                //listaDatos.get(0).setFkOcurrencia(fechaOcurrencia);


                i.putStringArrayListExtra("ESTUDIO", datosEstudio);
                i.putParcelableArrayListExtra("DATOS", listaDatos);

                setResult(RESULT_OK, i);
            } finally {
                finish();
            }

        }else{
            toast(error);
        }
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

    //Click en Dato
    @Override
    public void onButtonClickDatos() {
    }

    //Click en Ocurrencia
    @Override
    public void onButtonClickOcurrencia(String codOcurrencia, String nomEstudio) {
        verDatosOcurrencia(codOcurrencia, nomEstudio);
    }
}
