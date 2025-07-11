package com.merlita.diariolab;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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


public class AltaActivity extends AppCompatActivity
        implements AdaptadorTiposDato.OnButtonClickListener {
    private static final int DB_VERSION = MainActivity.DB_VERSION;
    EditText etTitulo, etDescripcion, etEmoji;
    Button btGuardar, btNuevoTipo;
    ArrayList<TipoDato> listaTiposDato = new ArrayList<>();
    ArrayList<Cualitativo> listaCualitativos = new ArrayList<>();
    AdaptadorTiposDato adaptadorTiposDato;
    RecyclerView rvTipos;
    String[] ordenSpinner = {"Número", "Texto", "Fecha", "Tipo"};
    private int posicionEdicion;
    private ArrayList<Integer> pillados = new ArrayList<>();


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
        setContentView(R.layout.activity_alta);

        etTitulo = findViewById(R.id.tvEstudioElegir);
        etDescripcion = findViewById(R.id.etDescripcion);
        etEmoji = findViewById(R.id.etEmoji);
        btGuardar = findViewById(R.id.btnGuardar);
        rvTipos = findViewById(R.id.recyclerTipos);


        btNuevoTipo = findViewById(R.id.btNuevoCualitativo);
        adaptadorTiposDato = new AdaptadorTiposDato(
                this, listaTiposDato, "", this);

        rvTipos.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));
        rvTipos.setAdapter(adaptadorTiposDato);


        actualizarLocal();


        btNuevoTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TipoDato nuevo = new TipoDato();
                int nuevoId = getNuevoIdTipo();
                nuevo.setId(nuevoId);
                listaTiposDato.add(0, nuevo);

                adaptadorTiposDato.notifyItemInserted(0);
            }
        });

    }

    private int getNuevoIdTipo() {
        int res=-1;


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null,  DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.estaElIDTipoLibre(db, 0, pillados);
            pillados.add(res);

            db.close();
        }

        return res;
    }
    private void actualizarLocal() {
        rvTipos.setLayoutManager(new LinearLayoutManager(this));
        rvTipos.setAdapter(adaptadorTiposDato);
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
                    String nombreEstudio = etTitulo.getText().toString();
                    datosEstudio.add(nombreEstudio);
                    datosEstudio.add(etDescripcion.getText().toString());
                    datosEstudio.add(etEmoji.getText().toString());

                    AdaptadorTiposDato a = (AdaptadorTiposDato) rvTipos.getAdapter();
                    assert a != null;
                    listaTiposDato = a.getLista();
                    for (int k = 0; k < listaTiposDato.size(); k++) {
                        TipoDato cambiar = listaTiposDato.get(k);
                        cambiar.setFkEstudio(nombreEstudio);
                        for (int j = 0; j < listaCualitativos.size(); j++) {
                            Cualitativo check = listaCualitativos.get(j);
                            if(check.getTitulo()==null){
                                listaCualitativos.remove(check);
                            }else{
                                check.setFk_dato_tipo_e(nombreEstudio);
                                if(check.getFk_dato_tipo_t().equals(cambiar.getNombre())){
                                    listaCualitativos.get(j).setFk_dato_tipo_t(cambiar.getNombre());
                                }
                            }
                        }

                    }


                    i.putStringArrayListExtra("ESTUDIO", datosEstudio);
                    i.putParcelableArrayListExtra("TIPOSDATO", listaTiposDato);
                    i.putParcelableArrayListExtra("CUALITATIVOS", listaCualitativos);

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

    //MENU CONTEXTUAL
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 121:
                //MENU --> BORRAR
                posicionEdicion = item.getGroupId();
                listaTiposDato.remove(posicionEdicion);
                adaptadorTiposDato.notifyItemRemoved(posicionEdicion);
                return true;
            default:
                return super.onContextItemSelected(item);
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
    public void onButtonClickNuevoCualitativo(Cualitativo nuevo) {
        listaCualitativos.add(nuevo);

    }
}
