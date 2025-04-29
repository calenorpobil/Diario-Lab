package com.merlita.diariolab;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorTiposDato;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;


public class AltaActivity extends AppCompatActivity
        implements AdaptadorTiposDato.OnButtonClickListener {
    private static final int DB_VERSION = MainActivity.DB_VERSION;
    EditText etTitulo, etDescripcion, etEmoji;
    Button btGuardar, btNuevoTipo;
    ArrayList<TipoDato> listaTiposDato = new ArrayList<>();
    AdaptadorTiposDato adaptadorTiposDato;
    RecyclerView vistaRecycler;
    String[] ordenSpinner = {"Número", "Texto", "Fecha"};


    private void toast(String e) {
        if(e!=null){
            Toast.makeText(this, e,
                    Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta);

        etTitulo = findViewById(R.id.tvEstudio2);
        etDescripcion = findViewById(R.id.etDescripcion);
        etEmoji = findViewById(R.id.etEmoji);
        btGuardar = findViewById(R.id.btnGuardar);
        vistaRecycler = findViewById(R.id.recyclerTipos);

        btNuevoTipo = findViewById(R.id.btNuevoTipoDato);
        adaptadorTiposDato = new AdaptadorTiposDato(this, listaTiposDato, this);


        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorTiposDato);


        actualizarLocal();


        btNuevoTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaTiposDato.add(new TipoDato());
                actualizarLocal();
            }
        });

    }

    private void actualizarLocal() {
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorTiposDato);
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
        vistaRecycler.setAdapter(adaptadorTiposDato);

    }


    private void rellenarLista(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from estudio", null);

        while (c.moveToNext()) {
            int index = c.getColumnIndex("NOMBRE");
            String nombre = c.getString(index);
            index = c.getColumnIndex("TIPO_DATO");
            String descripcion = c.getString(index);
            index = c.getColumnIndex("DESCRIPCION");
            String emoji = c.getString(index);
            listaTiposDato.add(new TipoDato(nombre, descripcion, emoji));
        }
        c.close();
    }


    public void clickGuardar(View v) {
        boolean correcto = true;
        if (!etEmoji.getText().toString().isEmpty() &&
                !etTitulo.getText().toString().isEmpty() &&
                !etDescripcion.getText().toString().isEmpty() &&
                !listaTiposDato.isEmpty()) {
            String error = comprobaciones(etTitulo, etEmoji, etDescripcion, listaTiposDato, "alta");

            if (error.isEmpty()) {
                Intent i = new Intent();

                //Valido campos obligatorios (según esquema SQL)
                try {
                    // Preparar todos los datos para enviar
                    ArrayList<String> datosEstudio = new ArrayList<>();
                    datosEstudio.add(etTitulo.getText().toString());
                    datosEstudio.add(etDescripcion.getText().toString());
                    datosEstudio.add(etEmoji.getText().toString());

                    AdaptadorTiposDato a = (AdaptadorTiposDato) vistaRecycler.getAdapter();
                    assert a != null;
                    listaTiposDato = a.getLista();

                    i.putStringArrayListExtra("ESTUDIO", datosEstudio);
                    i.putParcelableArrayListExtra("TIPOSDATO", listaTiposDato);

                    setResult(RESULT_OK, i);
                } finally {
                    finish();
                }

            }else{
                toast(error);
            }
        } else {
            toast("Rellena los datos o añade un Tipo de Dato. ");
        }
    }

    public static String comprobaciones(EditText etTitulo, EditText etEmoji,
                EditText etDescripcion, ArrayList<TipoDato> listaTiposDato, String alta) {
        String correcto = "";


        if(alta.equals("alta")){
            for (int i = 0; i < MainActivity.listaEstudios.size(); i++) {
                if(etTitulo.getText().toString().
                        equals(MainActivity.listaEstudios.get(i).getNombre())){
                    correcto = "Ese estudio ya existe. ";
                }
            }
        }

        if (etTitulo.getText().toString().isEmpty() ||
                etEmoji.getText().toString().isEmpty() ||
                etDescripcion.getText().toString().isEmpty()) {
            correcto = "Complete los campos obligatorios. ";
        }
        //Check por cada TipoDato
        for (int i = 0; i < listaTiposDato.size(); i++) {
            if (correcto.isEmpty()) {
                /*
                if (listaTiposDato.get(i).getNombre().equals(etTitulo.getText().toString())) {
                    correcto="El nombre de un dato no puede ser igual que el del Estudio. ";
                    break;
                }*/
                for (int j = 0; j < listaTiposDato.size(); j++) {
                    if (i != j && listaTiposDato.get(i).getNombre().
                            equals(listaTiposDato.get(j).getNombre())) {
                        correcto="No puedes llamar a dos datos con el mismo nombre. ";

                        break;
                    }
                }
                if (listaTiposDato.get(i).getNombre().isEmpty()) {
                    correcto="Tienes que poner un nombre al dato. ";
                    break;
                }
            }
        }
        return correcto;
    }



    @Override
    public void onButtonClick(int position) {

    }
}
