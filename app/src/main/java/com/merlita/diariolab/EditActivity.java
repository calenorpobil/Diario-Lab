package com.merlita.diariolab;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.merlita.diariolab.Modelos.TipoDato;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EditActivity extends AppCompatActivity
        implements AdaptadorTiposDato.OnButtonClickListener {

    private static final int DB_VERSION = 3;

    EditText etTitulo, etEmoji, etDescripcion;
    Button bt, btNuevoTipo;
    ArrayList<TipoDato> listaTiposDato = new ArrayList<>();
    AdaptadorTiposDato adaptadorTiposDato;
    RecyclerView vistaRecycler;
    String nombreEstudio;


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
                "where fk_estudio = ?", new String[]{nombreEstudio});

        while (c.moveToNext()) {
            int index = c.getColumnIndex("NOMBRE");
            String nombre = c.getString(index);
            index = c.getColumnIndex("TIPO_DATO");
            String tipoDato = c.getString(index);
            index = c.getColumnIndex("DESCRIPCION");
            String descripcion = c.getString(index);
            listaTiposDato.add(new TipoDato(nombre, tipoDato, descripcion));
        }
        c.close();
    }
    public void clickVolver(View v){
        Intent i = new Intent();

        // Obtengo referencias a todos los campos

        String nombre = etTitulo.getText().toString();
        String desc = etEmoji.getText().toString();
        int cuenta=-1;
        if(etDescripcion.getText().toString()!="")
            cuenta = Integer.parseInt(etDescripcion.getText().toString());
        try {
            i.putExtra("NOMBRE",  nombre);
            i.putExtra("DESCRIPCION", desc);
            i.putExtra("CUENTA", cuenta);

            setResult(RESULT_OK, i);
        } finally {
            finish();
        }
    }
    private void setupDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, day);
                        editText.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(selectedDate.getTime()));
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
    }



    @Override
    public void onButtonClick(int position) {

    }
}
