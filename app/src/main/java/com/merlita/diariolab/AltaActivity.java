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

import com.merlita.diariolab.Modelos.TipoDato;

import java.util.ArrayList;


public class AltaActivity extends AppCompatActivity
        implements AdaptadorTiposDato.OnButtonClickListener {
    private static final int DB_VERSION = 3;
    EditText etTitulo, etDescripcion, etEmoji;
    Button btGuardar, btNuevoTipo;
    ArrayList<TipoDato> listaTiposDato = new ArrayList<>();
    AdaptadorTiposDato adaptadorTiposDato;
    RecyclerView vistaRecycler;

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

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etEmoji = findViewById(R.id.etEmoji);
        btGuardar = findViewById(R.id.btnGuardar);
        vistaRecycler = findViewById(R.id.recyclerTipos);

        btNuevoTipo = findViewById(R.id.btNuevoTipoDato);
        adaptadorTiposDato = new AdaptadorTiposDato(this, listaTiposDato, this);


        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorTiposDato);

        if(listaTiposDato.isEmpty()){
            insertarInicial();
        }
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


    public void clickGuardar(View v){

        if(!etEmoji.getText().toString().isEmpty() &&
                !etTitulo.getText().toString().isEmpty() &&
                !etDescripcion.getText().toString().isEmpty() &&
                !listaTiposDato.isEmpty())
        {
            Intent i = new Intent();

            //Valido campos obligatorios (según esquema SQL)
            if (etTitulo.getText().toString().isEmpty() ||
                    etEmoji.getText().toString().isEmpty() ||
                    etDescripcion.getText().toString().isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios (*)",
                        Toast.LENGTH_SHORT).show();
            }else{
                try {
                    // Preparar todos los datos para enviar
                    String[] datosEstudio = {etTitulo.getText().toString(),
                            etEmoji.getText().toString()};
                    Object[] tipoDatos = listaTiposDato.toArray();

                    i.putExtra("ESTUDIO", datosEstudio);
                    i.putExtra("TIPOSDATO", tipoDatos);

                    setResult(RESULT_OK, i);
                } finally {
                    finish();
                }

            }

        }
    }


    @Override
    public void onButtonClick(int position) {

    }
}
