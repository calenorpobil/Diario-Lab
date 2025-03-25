package com.merlita.diariolab;

import static com.merlita.diariolab.AltaActivity.comprobaciones;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
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

public class EditActivity extends AppCompatActivity
        implements AdaptadorTiposDato.OnButtonClickListener {

    private static final int DB_VERSION = MainActivity.DB_VERSION;

    EditText etTitulo, etEmoji, etDescripcion;
    Button bt, btNuevoTipo;
    ArrayList<TipoDato>
            listaTiposDato  = new ArrayList<>(),
            listaAntigTiposDato = new ArrayList<>();
    AdaptadorTiposDato adaptadorTiposDato;
    RecyclerView vistaRecycler;
    String nombreEstudio;

    int posicion=-1;
    private boolean primeraVez=true;

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

        Bundle upIntent = this.getIntent().getExtras();
        assert upIntent != null;

        nombreEstudio = upIntent.getString("NOMBRE");
        String desc = upIntent.getString("DESCRIPCION");
        String emoji = upIntent.getString("EMOJI");
        posicion = upIntent.getInt("INDEX");
        etTitulo = findViewById(R.id.etTitulo);
        etEmoji = findViewById(R.id.etEmoji);
        etDescripcion = findViewById(R.id.etDescripcion);
        bt = findViewById(R.id.btnGuardar);
        vistaRecycler = findViewById(R.id.recyclerTipos);
        btNuevoTipo  = findViewById(R.id.btNuevoTipoDato);

        // Poblar campos de texto
        etTitulo.setText(nombreEstudio);
        etDescripcion.setText(desc);
        etEmoji.setText(emoji);

        adaptadorTiposDato = new AdaptadorTiposDato(this, listaTiposDato, this);


        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorTiposDato);



        actualizarDatos();


        btNuevoTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaTiposDato.add(new TipoDato());
                actualizarLocal();
            }
        });
        bt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View  view){
                clickEditar(view);
            }
        });

    }

    private void actualizarLocal() {
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorTiposDato);
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
        Cursor c = db.rawQuery("select * from DATO_TIPO " +
                "where FK_ESTUDIO = ?", new String[]{nombreEstudio});

        while (c.moveToNext()) {
            int index = c.getColumnIndex("NOMBRE");
            String nombre = c.getString(index);
            index = c.getColumnIndex("TIPO_DATO");
            String tipoDato = c.getString(index);
            index = c.getColumnIndex("DESCRIPCION");
            String descripcion = c.getString(index);
            listaTiposDato.add(new TipoDato(nombre, tipoDato, descripcion));
        }
        if(primeraVez) {
            listaAntigTiposDato = listaTiposDato;
            primeraVez=false;
        }

        c.close();
    }
    public void clickEditar(View v){
        if(!etEmoji.getText().toString().isEmpty() &&
                !etTitulo.getText().toString().isEmpty() &&
                !etDescripcion.getText().toString().isEmpty() &&
                !listaTiposDato.isEmpty())
        {
            String error = comprobaciones(etTitulo, etEmoji, etDescripcion, listaTiposDato,
                    "edit");

            if (error.isEmpty()) {
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
                        ArrayList<String> datosEstudio = new ArrayList<>();
                        datosEstudio.add(etTitulo.getText().toString());
                        datosEstudio.add(etDescripcion.getText().toString());
                        datosEstudio.add(etEmoji.getText().toString());


                        i.putStringArrayListExtra("ESTUDIO", datosEstudio);
                        i.putParcelableArrayListExtra("TIPOSDATO", listaAntigTiposDato);
                        i.putParcelableArrayListExtra("NUEVOSTIPOSDATO", listaTiposDato);
                        i.putExtra("INDEX", posicion);

                        setResult(RESULT_OK, i);
                    } finally {
                        finish();
                    }

                }

            }else{
                toast(error);
            }
        }else{
            toast("Rellena todos los campos. ");
        }
    }



    @Override
    public void onButtonClick(int position) {


    }
}
