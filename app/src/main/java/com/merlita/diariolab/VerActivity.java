package com.merlita.diariolab;

import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
        AdaptadorOcurrencias.OnButtonClickListener, AdaptadorTiposGrafico.OnButtonClickListener {
    private static final int DB_VERSION = MainActivity.DB_VERSION;

    TextView tvTitulo, tvSinDatos;
    Button btConfirmar, btModificar, btAnalizar;
    ArrayList<Ocurrencia> listaOcurrencias = new ArrayList<>();
    ArrayList<Dato> listaDatos = new ArrayList<>();
    ArrayList<TipoDato> listaTipos = new ArrayList<>();
    AdaptadorOcurrencias adaptadorOcurrencias;
    AdaptadorDatosVer adaptadorDatos;
    AdaptadorColumnas adaptadorColumnas;
    AdaptadorMedidas adaptadorMedidas;
    AdaptadorTiposGrafico adaptadorTiposGrafico;
    RecyclerView rvOcurrencias, rvDatos, rvMedidas, rvColumnas;
    RecyclerView rvTipos;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ver);

        Bundle upIntent = this.getIntent().getExtras();
        assert upIntent != null;

        estudioOcurrencia = upIntent.getParcelable("ESTUDIO");
        assert estudioOcurrencia != null;
        fk_estudio = estudioOcurrencia.getNombre();
        int numOcu = getOcurrencias();

        tvTitulo = findViewById(R.id.tvEstudioElegir);
        tvSinDatos = findViewById(R.id.tvSinDatos);
        btModificar = findViewById(R.id.btModificar);
        btConfirmar = findViewById(R.id.btConfirmar);
        btAnalizar = findViewById(R.id.btHome);
        rvOcurrencias = findViewById(R.id.rvOcurrencias);
        rvDatos = findViewById(R.id.rvAnalisis);
        rvColumnas = findViewById(R.id.rvGrafico);
        rvTipos = findViewById(R.id.rvTipos);
        rvMedidas = findViewById(R.id.rvInfo);
        tvTitulo.setText(estudioOcurrencia.getNombre());

        listaTipos = getTiposDato();
        if(fk_estudio!=null && numOcu>0 && !listaTipos.isEmpty()){



            listaDatos = getDatos(listaTipos, listaTipos.get(0).getFkEstudio());

            adaptadorOcurrencias = new AdaptadorOcurrencias(
                    this, estudioOcurrencia, listaOcurrencias, this);
            adaptadorTiposGrafico = new AdaptadorTiposGrafico(this, listaTipos, this);
            ArrayList<Dato> listaDatosTipo = getDatosDeTipo(listaTipos.get(0));
            adaptadorColumnas = new AdaptadorColumnas(this,
                    listaDatosTipo,
                    listaTipos.get(0));
            adaptadorMedidas = new AdaptadorMedidas(this, listaTipos, this);
            adaptadorMedidas = new AdaptadorMedidas(this, listaTipos, this);

            //Invertir el orden de las Ocurrencias:
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            rvOcurrencias.setLayoutManager(new LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL,
                    true));
            rvOcurrencias.setAdapter(adaptadorOcurrencias);

            rvTipos.setLayoutManager(new LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL,
                    false));
            rvTipos.setAdapter(adaptadorTiposGrafico);


            rvColumnas.setLayoutManager(new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL,
                    false));
            rvColumnas.setAdapter(adaptadorColumnas);

            rvMedidas.setLayoutManager(new LinearLayoutManager(this));
            rvMedidas.setAdapter(adaptadorMedidas);


            actualizarRvDatos();

        }else{
            //NO HA FUNCIONADO EL CODIGO
            tvSinDatos.setVisibility(VISIBLE);
        }

        actualizarListas();


        btModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ocurrencia !=null){
                    enabled = !enabled;
                    verDatosOcurrencia(ocurrencia, fk_estudio, enabled);
                    if(enabled){
                        btModificar.setText(R.string.cancelar);
                    }else btModificar.setText(R.string.modificar);
                }


            }
        });

        btConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enabled){
                    borrarDatosOcurrencia(ocurrencia);
                    insertarDatos(listaDatos);
                    enabled = !enabled;
                    listaDatos = getDatos(listaTipos, listaTipos.get(0).getFkEstudio());
                    verDatosOcurrencia(ocurrencia, fk_estudio, enabled);
                    btModificar.setText(R.string.modificar);
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


    public void adjustRecyclerViewHeight(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) return;

        int totalHeight = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                recyclerView.getWidth(),
                View.MeasureSpec.EXACTLY
        );

        // Medición UNSPECIFIED para altura dinámica
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
        );

        for (int i = 0; i < adapter.getItemCount(); i++) {
            RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));
//            adapter.onBindViewHolder(holder, i);
            holder.itemView.measure(widthMeasureSpec, heightMeasureSpec);
            totalHeight += holder.itemView.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = totalHeight + recyclerView.getPaddingTop() + recyclerView.getPaddingBottom();
        recyclerView.setLayoutParams(params);
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
                // MENU --> BORRAR
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("⚠");
                builder.setMessage("Esta Ocurrencia tiene datos asignados. " +
                        "¿Seguro que quieres borrarlos?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Ocurrencia oc = listaOcurrencias.get(position);
                        listaOcurrencias.remove(position);
                        adaptadorOcurrencias.notifyItemRemoved(position);
                        borrarOcurrencia(oc, position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            case 122:
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void borrarOcurrencia(Ocurrencia oc, int position) {
        int res = -1;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                res = usdbh.borrarocurrencia_PorID(db, oc);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        adaptadorOcurrencias.notifyItemRemoved(position);

    }
    private void borrarDatosOcurrencia(Ocurrencia ocu) {
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                usdbh.borrarDatos_PorOcurrencia(db, ocu);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
    }

    private void insertarDatos(ArrayList<Dato> datos) {
        long res = 0;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);){
            SQLiteDatabase db = usdbh.getWritableDatabase();

            for (int i = 0; i < datos.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("FK_TIPO_N", datos.get(i).getFkTipoDato());
                values.put("FK_TIPO_E", datos.get(i).getFkTipoEstudio());
                values.put("FK_OCURRENCIA", datos.get(0).getFkOcurrencia());
                String valor = datos.get(i).getValorText();
                if(valor.isEmpty() || valor.equals("Sin datos")){
                    values.put("VALOR_TEXT", " ");
                }else{
                    values.put("VALOR_TEXT", valor);
                }

                res = db.insert("Dato", null,
                        values);

            }

            db.close();
        } catch (SQLiteException ex){
            toast(ex.getMessage());
        }
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
        if(adaptadorOcurrencias != null && adaptadorColumnas != null){
            adaptadorOcurrencias.notifyItemRangeChanged(sizeOcurrencias, added);
            adaptadorColumnas.notifyItemRangeChanged(sizeOcurrencias, added);
        }
    }



    private ArrayList<Dato> getDatosOcurrencia(ArrayList<TipoDato> listaTipos,
                                               Ocurrencia ocurrencia, String nomEstudio) {
        ArrayList<Dato> datosResultado = new ArrayList<>();
        ArrayList<Dato> datosAux = new ArrayList<>();
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                datosAux = usdbh.getDatosPorOcurrencia(db, ocurrencia.getCod(), nomEstudio);

                db.close();
            } catch (Exception ex){
                toast("Juan");
                Log.d("MyAdapter", ex.getMessage());
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }

        for (TipoDato td:
                listaTipos) {
            for (Dato d :
                    datosAux) {
                if (d.getFkTipoDato().equals(td.getId()+"")) {
                    datosResultado.add(d);
                } else {
//                    datosResultado.add(new Dato());
                }
            }
        }


        return datosResultado;
    }
    private int getOcurrencias() {
        int datosResultado=-1;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                datosResultado = usdbh.getCuentaOcurrencias(db, fk_estudio);

                db.close();
            } catch (Exception ex){
                toast("Juan");
                Log.d("MyAdapter", ex.getMessage());
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        return datosResultado;
    }

    private ArrayList<Dato> getDatos(ArrayList<TipoDato> listaTipos, String nomEstudio) {
        ArrayList<Dato> datosResultado =new ArrayList<>();
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
                Log.d("MyAdapter", ex.getMessage());
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        return datosResultado;
    }


    private void verDatosOcurrencia(Ocurrencia codOcurrencia, String nomEstudio, boolean enabled) {
        listaTipos = getTiposDato();
        listaDatos = getDatosOcurrencia(listaTipos, codOcurrencia, nomEstudio);

        adaptadorDatos = new AdaptadorDatosVer(
                this, listaTipos, this, ocurrencia,
                listaDatos, this, enabled);

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




        }
        String titulo = estudioOcurrencia.getEmoji()+" "+fk_estudio;
        tvTitulo.setText(titulo);

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

    private ArrayList<Dato> getDatosDeTipo(TipoDato tipo) {
        ArrayList<Dato> datosResultado =new ArrayList<>();
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                datosResultado = usdbh.getDatosDeTipo(db, tipo.getFkEstudio(), tipo.getNombre());

                db.close();
            } catch (Exception ignored){
            }
        } catch (SQLiteDatabaseCorruptException ignored){
        }
        return datosResultado;
    }

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
    public void onButtonClickDatos(ArrayList<Dato> listaDatos) {
        actualizarListaDatos(listaDatos);
    }

    private void actualizarListaDatos(ArrayList<Dato> listaDatos) {
        this.listaDatos = listaDatos;
    }
    @Override
    public void onButtonClickTipoGrafico(TipoDato tipoDato) {
        listaDatos = getDatosDeTipo(tipoDato);
        rvColumnas.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false));
        adaptadorColumnas = new AdaptadorColumnas(this, listaDatos, tipoDato);
        rvColumnas.setAdapter(adaptadorColumnas);
    }

}
