package com.merlita.diariolab;

import static com.merlita.diariolab.AltaActivity.comprobaciones;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.merlita.diariolab.Adaptadores.AdaptadorTiposDato;
import com.merlita.diariolab.Modelos.Cualitativo;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity
        implements AdaptadorTiposDato.OnButtonClickListener {

    private static final int DB_VERSION = MainActivity.DB_VERSION;

    EditText etTitulo, etEmoji, etDescripcion;
    Button bt, btNuevoTipo;
    ArrayList<TipoDato>
            listaTiposDato  = new ArrayList<>();
    private ArrayList<Cualitativo> listaCualitativos = new ArrayList<>();
    AdaptadorTiposDato adaptadorTiposDato;
    RecyclerView rvTipos;
    String nombreEstudio;
    int posicion=-1;
    private boolean primeraVez=true;
    int posicionEdicion;


    private void toast(String e) {
        if(e!=null){
            Toast.makeText(this, e,
                    Toast.LENGTH_SHORT).show();
        }
    }

    //MENU CONTEXTUAL
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 121:
                //MENU --> BORRAR
                Intent i = new Intent(this, EditActivity.class);
                posicionEdicion = item.getGroupId();
                listaTiposDato.remove(posicionEdicion);
                adaptadorTiposDato.notifyItemRemoved(posicionEdicion);
                return true;
            default:
                return super.onContextItemSelected(item);
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
        etTitulo = findViewById(R.id.tvEstudioElegir);
        etEmoji = findViewById(R.id.etEmoji);
        etDescripcion = findViewById(R.id.etDescripcion);
        bt = findViewById(R.id.btnGuardar);
        rvTipos = findViewById(R.id.recyclerTipos);
        btNuevoTipo  = findViewById(R.id.btNuevoCualitativo);

        // Poblar campos de texto
        etTitulo.setText(nombreEstudio);
        etDescripcion.setText(desc);
        etEmoji.setText(emoji);


        adaptadorTiposDato = new AdaptadorTiposDato(this,
                listaTiposDato, nombreEstudio, this);


        rvTipos.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));
        rvTipos.setAdapter(adaptadorTiposDato);



        actualizarDatos();


        btNuevoTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaTiposDato.add(0, new TipoDato());
                adaptadorTiposDato.notifyItemInserted(0);
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
        rvTipos.setLayoutManager(new LinearLayoutManager(this));
        rvTipos.setAdapter(adaptadorTiposDato);
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
            toast("Inténtalo en otro momento. ");
        }
        rvTipos.setLayoutManager(new LinearLayoutManager(this));
        rvTipos.setAdapter(adaptadorTiposDato);

    }

    private void rellenarLista(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from DATO_TIPO " +
                "where FK_ESTUDIO = ?", new String[]{nombreEstudio});

        while (c.moveToNext()) {
            //TODO: recoger el FK_estudio
            int index = c.getColumnIndex("NOMBRE");
            String nombre = c.getString(index);
            index = c.getColumnIndex("TIPO_DATO");
            String tipoDato = c.getString(index);
            index = c.getColumnIndex("DESCRIPCION");
            String descripcion = c.getString(index);
            TipoDato nuevo = new TipoDato(nombre, tipoDato, descripcion);
            recuperarCualitativos(db, nuevo);
            listaTiposDato.add(0, nuevo);
            adaptadorTiposDato.notifyItemInserted(0);
        }

        c.close();
    }
    private void recuperarCualitativos(SQLiteDatabase db, TipoDato nuevo) {
        Cursor c = db.rawQuery("select * from CUALITATIVO " +
                "where FK_TIPO_DATO_T = ? and FK_TIPO_DATO_E = ?", new
                String[]{nuevo.getNombre(), nombreEstudio});

        while (c.moveToNext()) {
            int index = c.getColumnIndex("TITULO");
            String titulo = c.getString(index);
            nuevo.setFkEstudio(nombreEstudio);
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
                        for (int j = 0; j < listaCualitativos.size(); j++) {
                            listaCualitativos.get(j).setFk_dato_tipo_e(datosEstudio.get(0));
                        }
                        for (int j = 0; j < listaTiposDato.size(); j++) {
                            listaTiposDato.get(j).setFkEstudio(datosEstudio.get(0));
                        }


                        i.putStringArrayListExtra("ESTUDIO", datosEstudio);
                        i.putParcelableArrayListExtra("NUEVOSTIPOSDATO", listaTiposDato);
                        i.putParcelableArrayListExtra("NUEVOSCUALITATIVOS", listaCualitativos);
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
    public void onButtonClickNuevoCualitativo(Cualitativo nuevo) {
        listaCualitativos.add(nuevo);
    }
}
