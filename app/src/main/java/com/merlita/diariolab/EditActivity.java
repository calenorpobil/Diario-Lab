package com.merlita.diariolab;

import static com.merlita.diariolab.AltaActivity.comprobaciones;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
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
import com.merlita.diariolab.Modelos.Dato;
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
    ArrayList<TipoDato>
            listaAnterior  = new ArrayList<>();
    ArrayList<TipoDato> tiposCambiados = new ArrayList<>();

    private ArrayList<Cualitativo> listaCualitativos = new ArrayList<>();
    AdaptadorTiposDato adaptadorTiposDato;
    RecyclerView rvTipos;
    String nombreEstudio, nombreEstudioAnterior;
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
        int pos = item.getGroupId();

        if (item.getItemId() == 121) {//MENU --> BORRAR

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("⚠");
            builder.setMessage("Este Tipo tiene datos asignados. " +
                    "¿Seguro que quieres borrarlos todos?");
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TipoDato tipoPorBorrar = listaTiposDato.get(posicionEdicion);

                    listaTiposDato.remove(posicionEdicion);
                    adaptadorTiposDato.notifyItemRemoved(posicionEdicion);
                    borrarDatosTipos(tipoPorBorrar);
                    //borrar el tipo en SQL
                    //borrar tipos con el dato.
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
            return true;
        }else if (item.getItemId() == 122) {
            listaCualitativos.remove(pos);
            actualizarDatos();

        }
        return super.onContextItemSelected(item);
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alta);

        Bundle upIntent = this.getIntent().getExtras();
        assert upIntent != null;

        nombreEstudioAnterior = upIntent.getString("NOMBRE");
        nombreEstudio = nombreEstudioAnterior;
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
        listaAnterior = TipoDato.copiaPorValor(listaTiposDato);



        btNuevoTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cuentaTipos = getCuentaTipos();
                TipoDato tdNuevo = new TipoDato();
                tdNuevo.setId(cuentaTipos+1);

                listaTiposDato.add(0, tdNuevo);
                adaptadorTiposDato.notifyItemInserted(0);
                rvTipos.scrollToPosition(0);
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

    public void borrarDatosTipos(TipoDato tipo) {
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                tipo.setFkEstudio(nombreEstudio);

                usdbh.borrarOcurrenciasVacias(tipo);
                usdbh.borrarTipoDato(tipo);
                usdbh.borrarDatos_porTipo(tipo);


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
            index = c.getColumnIndex("ID");
            int id = c.getInt(index);
            TipoDato nuevo = new TipoDato(id, nombre, tipoDato, descripcion);
            recuperarCualitativos(db, nuevo);
            listaTiposDato.add(0, nuevo);
            adaptadorTiposDato.notifyItemInserted(0);
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

    private void recuperarCualitativos(SQLiteDatabase db, TipoDato nuevo) {
        Cursor c = db.rawQuery("select * from CUALITATIVO " +
                "where FK_TIPO_DATO_T = ? and FK_TIPO_DATO_E = ?", new
                String[]{nuevo.getId()+"", nombreEstudio});

        while (c.moveToNext()) {
            int index = c.getColumnIndex("TITULO");
            String titulo = c.getString(index);
            nuevo.setFkEstudio(nombreEstudio);
        }

        c.close();
    }
    public void clickEditar(View v){
        nombreEstudio = etTitulo.getText().toString();
        if(!etEmoji.getText().toString().isEmpty() &&
                !etTitulo.getText().toString().isEmpty() &&
                !etDescripcion.getText().toString().isEmpty() &&
                !listaTiposDato.isEmpty())
        {
            tiposCambiados = unTipoHaCambiado();
            String mensajeCambiados = mensajeCambiados();
            boolean cualitativoSinTitulo = isCualitativoSinTitulo();
            if(!tiposCambiados.isEmpty()){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("⚠");
                builder.setMessage("No se puede cambiar el Tipo de dato de "+mensajeCambiados+" " +
                        "porque contiene datos. Bórralo y crea uno nuevo. ");
                builder.setPositiveButton("Vale", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
            } else {
                if(isCualitativoSinTitulo()){

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("⚠");
                    builder.setMessage("Todos los Cualitativos deben tener un título. ");
                    builder.setPositiveButton("Vale", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();

                }else{
                    guardarDatos();
                }
            }
        }else{
            toast("Rellena todos los campos. ");
        }
    }

    private boolean isCualitativoSinTitulo() {
        for (Cualitativo c :
                listaCualitativos) {
            if (c.getTitulo()==null) {
                return true;
            }
        }
        return false;
    }

    private void guardarDatos(){
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
                if(tituloEstudioRepetido()){
                    Toast.makeText(this, "El nombre del Estudio ya existe, cámbielo. ",
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
                        i.putParcelableArrayListExtra("TIPOSANTERIORES", listaAnterior);
                        i.putParcelableArrayListExtra("NUEVOSCUALITATIVOS", listaCualitativos);
                        i.putExtra("INDEX", posicion);

                        setResult(RESULT_OK, i);
                    } finally {
                        finish();
                    }
                }
            }

        }else{
            toast(error);
        }
    }

    private String mensajeCambiados() {
        String nombre = "";
        for (int i = 0; i < tiposCambiados.size(); i++) {
            if(i==tiposCambiados.size()-1 && tiposCambiados.size()!=1){
                nombre += " y "+tiposCambiados.get(i).getNombre();
            } else if(tiposCambiados.size()!=1 && i==0){
                nombre += tiposCambiados.get(i).getNombre();
            } else if ( tiposCambiados.size()!=1){
                nombre += ", "+tiposCambiados.get(i).getNombre();
            }else{
                nombre += tiposCambiados.get(i).getNombre();
            }
        }
        return nombre;
    }

    private boolean tituloEstudioRepetido() {
        boolean res = false;
        ArrayList<String> nombresEstudios = new ArrayList<>();

        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                nombresEstudios = usdbh.getListaNombreEstudios(db);
                nombresEstudios.remove(nombreEstudioAnterior);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Inténtalo en otro momento. ");
        }

        for (String nombre :
                nombresEstudios) {
            if (nombre.equals(nombreEstudio)){
                res = true;
                break;
            }
        }

        return res;
    }

    private ArrayList<TipoDato> unTipoHaCambiado() {
        ArrayList<TipoDato> tiposCambiados = new ArrayList<>();

        for (TipoDato tipo :
                listaTiposDato) {
            int index = listaAnterior.indexOf(tipo);

            if (index>-1){
                TipoDato anterior = listaAnterior.get(index);
                if(!tipo.getTipoDato().equals(anterior.getTipoDato())){
                    tiposCambiados.add(tipo);
                }
            }
        }


        return tiposCambiados;
    }


    @Override
    public void onButtonClickNuevoCualitativo(Cualitativo nuevo) {
        listaCualitativos.add(0, nuevo);
    }
}
