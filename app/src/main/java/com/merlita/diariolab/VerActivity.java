package com.merlita.diariolab;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorDatosVer;
import com.merlita.diariolab.Adaptadores.AdaptadorEstudios;
import com.merlita.diariolab.Adaptadores.AdaptadorColumnas;
import com.merlita.diariolab.Adaptadores.AdaptadorMedidas;
import com.merlita.diariolab.Adaptadores.AdaptadorOcurrencias;
import com.merlita.diariolab.Adaptadores.AdaptadorTiposGrafico;
import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.time.LocalDate;
import java.util.ArrayList;


public class VerActivity extends AppCompatActivity
        implements AdaptadorDatosVer.OnButtonClickListener, AdaptadorDatosVer.DatePickerListener,
        AdaptadorOcurrencias.OnButtonClickListener, AdaptadorTiposGrafico.OnButtonClickListener,
        AdaptadorColumnas.OnButtonClickListener {
    private static final int DB_VERSION = MainActivity.DB_VERSION;

    TextView tvTitulo;
    Button btGuardar, btConfirmar, btModificar, btAnalizar;
    ArrayList<Ocurrencia> listaOcurrencias = new ArrayList<>();
    ArrayList<Dato> listaDatos = new ArrayList<>();
    ArrayList<TipoDato> listaTipos = new ArrayList<>();
    AdaptadorOcurrencias adaptadorOcurrencias;
    AdaptadorDatosVer adaptadorDatos;
    AdaptadorColumnas adaptadorColumnas;
    AdaptadorMedidas adaptadorMedidas;
    AdaptadorTiposGrafico adaptadorTipos;
    RecyclerView rvOcurrencias, rvDatos, rvTipos, rvMedidas, rvColumnas;
    private Ocurrencia ocurrencia;
    private LocalDate fechaOcurrencia;
    private int posicion;
    Estudio estudioOcurrencia;
    private String fk_estudio;
    private int reps = 1;
    boolean enabled = false;





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
            tvTitulo = findViewById(R.id.tvEstudioElegir);
            btGuardar = findViewById(R.id.btnGuardar);
            btModificar = findViewById(R.id.btModificar);
            btConfirmar = findViewById(R.id.btConfirmar);
            btAnalizar = findViewById(R.id.btHome);
            rvOcurrencias = findViewById(R.id.rvOcurrencias);
            rvDatos = findViewById(R.id.rvAnalisis);
            rvColumnas = findViewById(R.id.rvGrafico);
            rvTipos = findViewById(R.id.rvTipos);
            rvMedidas = findViewById(R.id.rvInfo);


            /*rvGrafico.setLayoutManager(new LinearLayoutManager(this));
            rvOcurrencias.setAdapter(adaptadorGrafico);

            rvMedidas.setLayoutManager(new LinearLayoutManager(this));
            rvOcurrencias.setAdapter(adaptadorMedidas);*/

            tvTitulo.setText(estudioOcurrencia.getNombre());

            listaTipos = getTiposDato();
            listaDatos = getDatos(listaTipos.get(0).getFkEstudio());

            adaptadorOcurrencias = new AdaptadorOcurrencias(
                    this, estudioOcurrencia, listaOcurrencias, this);
            adaptadorTipos = new AdaptadorTiposGrafico(this, listaTipos, this);
            adaptadorColumnas = new AdaptadorColumnas(this,
                    listaOcurrencias,
                    listaDatos,
                    listaTipos, this);

            //Invertir el orden de las Ocurrencias:
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            rvOcurrencias.setLayoutManager(linearLayoutManager);
            rvOcurrencias.setLayoutManager(new LinearLayoutManager(this));
            rvOcurrencias.setAdapter(adaptadorOcurrencias);

            rvTipos.setLayoutManager(new LinearLayoutManager(this));
            rvTipos.setAdapter(adaptadorTipos);

            rvColumnas.setLayoutManager(new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL,
                    false));
            rvColumnas.setAdapter(adaptadorColumnas);


            actualizarRvDatos();

        }else{
            //NO HA FUNCIONADO EL CODIGO
        }

        actualizarListas();


        btModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ocurrencia !=null){
                    enabled = !enabled;
                    verDatosOcurrencia(ocurrencia, fk_estudio, enabled);
                    if(enabled){
                        btModificar.setText("Cancelar");
                    }else btModificar.setText("Modificar");
                }


            }
        });


        btAnalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(VerActivity.this, AnalisisActivity.class);
                i.putExtra("ESTUDIO", estudioOcurrencia.getNombre());
                lanzadorAlta.launch(i);

            }
        });


    }

    private void actualizarRvDatos() {
        rvDatos.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false));
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


    //MENU CONTEXTUAL
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item){
        int position = item.getGroupId();
        switch(item.getItemId())
        {
            case 121:
                //MENU --> BORRAR
                Ocurrencia oc = listaOcurrencias.get(position);
                listaOcurrencias.remove(position);
                adaptadorOcurrencias.notifyItemRemoved(position);
                borrarOcurrencia(oc);
            case 122:
            default:
                return super.onContextItemSelected(item);
        }
    }

    private int borrarOcurrencia(Ocurrencia oc) {
        int res = -1;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                res = usdbh.borrarocurrencia_PorID(db, oc.getCod(), oc.getFkEstudioN());

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }

        return res;
    }


    public void actualizarListas() {
        int sizeOcurrencias = listaOcurrencias.size();
        int added=0;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                listaOcurrencias.clear();

                added = rellenarLista(db);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        //rvOcurrencias.setLayoutManager(new LinearLayoutManager(this));
        //rvOcurrencias.setAdapter(adaptadorOcurrencias);
        adaptadorOcurrencias.notifyItemRangeChanged(sizeOcurrencias, added);
        adaptadorColumnas.notifyItemRangeChanged(sizeOcurrencias, added);

    }



    private ArrayList<Dato> getDatosOcurrencia(Ocurrencia codOcurrencia, String nomEstudio) {
        ArrayList<Dato> datosResultado=null;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                datosResultado = usdbh.getDatosPorOcurrencia(db, ocurrencia.getCod(), nomEstudio);

                db.close();
            } catch (Exception ex){
                toast("Juan");
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        return datosResultado;
    }

    private ArrayList<Dato> getDatos(String nomEstudio) {
        ArrayList<Dato> datosResultado=null;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                datosResultado = usdbh.getDatos(db, nomEstudio);

                db.close();
            } catch (Exception ex){
                toast("Juan");
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        return datosResultado;
    }


    private void verDatosOcurrencia(Ocurrencia codOcurrencia, String nomEstudio, boolean enabled) {
        listaDatos = getDatosOcurrencia(codOcurrencia, nomEstudio);
        listaTipos = getTiposDato();

        adaptadorDatos = new AdaptadorDatosVer(
                this, listaDatos, this, ocurrencia,
                listaTipos, this, enabled);

        rvDatos.setAdapter(adaptadorDatos);

        //actualizarListas();
        actualizarRvDatos();
    }
    private int rellenarLista(SQLiteDatabase db) {
        int res = 0;
        LocalDate fecha=null;


        Cursor c = db.rawQuery("select * from ocurrencia " +
                "where FK_ESTUDIO_N = ?",
                new String[]{estudioOcurrencia.getNombre()});

        while (c.moveToNext()) {
            res++;
            int index=0;
            index = c.getColumnIndex("FECHA");
            String par = c.getString(index);
            fecha = LocalDate.parse(par);
            index = c.getColumnIndex("ID");
            String id = c.getString(index);

            Ocurrencia ver = new Ocurrencia(id, fecha, estudioOcurrencia.getNombre());

            listaOcurrencias.add(ver);



            tvTitulo.setText(estudioOcurrencia.getEmoji()+" "+fk_estudio);

        }

        c.close();
        return res;
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

    ActivityResultLauncher<Intent>
            lanzadorAlta = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult resultado) {
                    if(resultado.getResultCode()==RESULT_OK) {

                    }else{
                        //SIN DATOS
                    }
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {

            }
        } else if (requestCode == 2) {

            if (resultCode == RESULT_OK) {

            }
        } else if (requestCode == 3) {
        }
    }


    // Click en Ocurrencia
    @Override
    public void onButtonClickOcurrencia(Ocurrencia ocurrencia) {
        this.ocurrencia = ocurrencia;
        btModificar.setText("Modificar");
        enabled = false;
        verDatosOcurrencia(this.ocurrencia, ocurrencia.getFkEstudioN(), enabled);
    }

    // Click en Datos
    @Override
    public void onButtonClickDatos() {

    }

    @Override
    public void onButtonClickTipo() {
        tvTitulo.setText(tvTitulo.getText()+"1");
    }
}
