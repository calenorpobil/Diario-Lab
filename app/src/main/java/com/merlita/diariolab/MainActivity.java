package com.merlita.diariolab;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;
import static com.merlita.diariolab.Utils.copiarArchivo;
import static com.merlita.diariolab.Utils.multiBoolean;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static RecyclerView vistaRecycler;
    public static ArrayList<Estudio> listaEstudios = new ArrayList<Estudio>();
    TextView tv, tvErrores;
    static AdaptadorEstudios adaptadorEstudios;
    Button btAlta, btDev, btRevert;


    public final static int DB_VERSION=5;



    private Uri selectedFileUri;



    File database = new File(
            Environment.getDataDirectory()+
                    "/data/com.merlita.diariolab/databases/"+"DBEstudios");
    File bk_database = new File(
            Environment.getDataDirectory()+
                    "/data/com.merlita.diariolab/files/"+"bk_DBEstudios");
    File database_server = new File(
            Environment.getDataDirectory()+
                    "/data/com.merlita.diariolab/databases/"+"server_DBEstudios");



    private static final String SERVIDOR_IP = "10.0.2.2";
    //10.0.2.2      LOCALHOST
    //172.17.0.1     LINUX
    private static final int PUERTO = 8888;



    private void toast(String e) {
        if(e!=null){
            Toast.makeText(this, e,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //EdgeToEdge.enable(this);

        tv = findViewById(R.id.tvTitulo);
        btAlta = findViewById(R.id.btAlta);
        btDev = findViewById(R.id.btCopia);
        btRevert = findViewById(R.id.btRevert);
        vistaRecycler = findViewById(R.id.recyclerView);
        adaptadorEstudios = new AdaptadorEstudios(this, listaEstudios);

        tvErrores = findViewById(R.id.tvErrores);


        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(VERTICAL);
        lm.setReverseLayout(true);

        vistaRecycler.setLayoutManager(lm);
        vistaRecycler.setAdapter(adaptadorEstudios);



        //borrarTodo();
        insertarDatosIniciales();
        actualizarDatos();


        btAlta.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //ONCLICK

                Intent i = new Intent(MainActivity.this, AltaActivity.class);
                lanzadorAlta.launch(i);

                //sumarNumerosServer();

            }
        });

        btRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pone archivo en carpeta files.



                openFolderPickerIn();




            }
        });

        btDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openFolderPickerOut();
                //enviarArchivoNube();


            }
        });
    }

    //PROCESS SELECTED FILE TO BACKUP
    private void handleFileSelection(Uri fileUri) {
        FileHelper fileHelper = new FileHelper(this);

        try{
            fileHelper.copiarArchivoDeUri(fileUri, bk_database);
            copiarArchivo(bk_database, database);
            actualizarDatos();

        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    handleFileSelection(selectedFileUri);
                }
            }
    );

    private void openFolderPickerOut() {
        // Intent para abrir el selector de carpetas
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
    }



    private void openFolderPickerIn(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*");  // Para seleccionar cualquier tipo de archivo

        filePickerLauncher.launch(intent);
    }


    private Uri selectedFolderUri;
    private String selectedFileName;


    private final ActivityResultLauncher<Intent> folderPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Obtener la URI de la carpeta seleccionada
                    selectedFolderUri = result.getData().getData();
                    showFileNameDialog();
                }
            }
    );

    private void showFileNameDialog() {
        // Crear un diálogo para ingresar el nombre del archivo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nombre del archivo");

        final EditText input = new EditText(this);
        input.setHint("Ingresa el nombre del archivo");
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            selectedFileName = input.getText().toString().trim();
            if (!selectedFileName.isEmpty()) {
                saveFile();
            } else {
                Toast.makeText(this, "Debes ingresar un nombre de archivo", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveFile() {
        File content = database;

        if (!content.exists()) {
            Toast.makeText(this, "El contenido no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Asegurarse de que el nombre del archivo tenga extensión .db
        if (!selectedFileName.endsWith(".db")) {
            selectedFileName += ".db";
        }

        // Guardar el archivo usando FileHelper
        FileHelper fileHelper = new FileHelper(this);
        boolean success = fileHelper.saveFile(selectedFolderUri, database, selectedFileName);

        if (success) {
            Toast.makeText(this, "Archivo guardado con éxito", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }
    }


    private void backupLocalCopiar() {
        try {
            copiarArchivo(database, bk_database);
            toast("Backup local hecha. ");
        } catch (IOException e) {
            toast("No se ha podido hacer la backup local. Vuelve a intentarlo. ");
        }
    }

    public static void actualizarLocal(){
        vistaRecycler.setAdapter(adaptadorEstudios);

    }

    public void actualizarDatos() {
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                //usdbh.onUpgrade(db, 4, DB_VERSION);
                listaEstudios.clear();

                rellenarLista(db);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorEstudios);

    }






    public void insertarDatosIniciales() {
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);) {


            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Iniciar una transacción para mejorar el rendimiento
            try {
                db.beginTransaction();
                // Insertar estudios
                usdbh.insertarEstudio(db, new Estudio("Tomar Café", "Registro de consumo diario de café", "☕"));
                usdbh. insertarEstudio(db,new Estudio("Ir al gimnasio", "Seguimiento de sesiones de entrenamiento", "\uD83C\uDFCB\uFE0F"));
                usdbh. insertarEstudio(db,new Estudio("Diario", "Registro personal diario", "\uD83D\uDCD4"));

                // Insertar tipos de datos para "Tomar Café"
                usdbh.insertarTipoDato(db,new TipoDato("Tazas", "Número", "Cantidad de tazas consumidas", "Tomar Café"));
                usdbh.insertarTipoDato(db,new TipoDato("Hora", "Fecha", "Hora en que se tomó el café", "Tomar Café"));
                usdbh.insertarTipoDato(db,new TipoDato("Tipo de café", "Texto", "Tipo de café consumido", "Tomar Café"));

                // Tipos para "Ir al gimnasio"
                usdbh.insertarTipoDato(db,new TipoDato("Duración", "Número", "Duración del entrenamiento en minutos", "Ir al gimnasio"));
                usdbh.insertarTipoDato(db,new TipoDato("Fecha", "Fecha", "Fecha del entrenamiento", "Ir al gimnasio"));
                usdbh.insertarTipoDato(db,new TipoDato("Actividad", "Texto", "Tipo de actividad realizada", "Ir al gimnasio"));

                // Tipos para "Diario"
                usdbh.insertarTipoDato(db,new TipoDato("Estado de ánimo", "Texto", "Descripción del estado de ánimo", "Diario"));
                usdbh.insertarTipoDato(db,new TipoDato("Fecha", "Fecha", "Fecha del registro", "Diario"));
                usdbh.insertarTipoDato(db,new TipoDato("Evento destacado", "Texto", "Evento importante del día", "Diario"));

                // Insertar ocurrencias para "Tomar Café"
                // Ocurrencias para "Tomar Café"
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-01T08:30:00"), "Tomar Café"));
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-02T09:15:00"), "Tomar Café"));
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-03T07:45:00"), "Tomar Café"));

                // Ocurrencias para "Ir al gimnasio"
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-01T18:00:00"), "Ir al gimnasio"));
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-03T19:30:00"), "Ir al gimnasio"));
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-05T17:45:00"), "Ir al gimnasio"));

                // Ocurrencias para "Diario"
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-01T22:00:00"), "Diario"));
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-02T21:30:00"), "Diario"));
                usdbh. insertarOcurrencia(db,new Ocurrencia(LocalDateTime.parse("2024-03-03T23:00:00"), "Diario"));

                // Insertar datos para "Tomar Café"
                // Datos para "Tomar Café"
                usdbh. insertarDato(db,new Dato("Tazas", "Tomar Café", LocalDateTime.parse("2024-03-01T08:30:00"), "2"));
                usdbh. insertarDato(db,new Dato("Hora", "Tomar Café", LocalDateTime.parse("2024-03-01T08:30:00"), "2024-03-01T08:30:00"));
                usdbh. insertarDato(db,new Dato("Tipo de café", "Tomar Café", LocalDateTime.parse("2024-03-01T08:30:00"), "Espresso"));
                usdbh. insertarDato(db,new Dato("Tazas", "Tomar Café", LocalDateTime.parse("2024-03-02T09:15:00"), "1"));
                usdbh. insertarDato(db,new Dato("Hora", "Tomar Café", LocalDateTime.parse("2024-03-02T09:15:00"), "2024-03-02T09:15:00"));
                usdbh. insertarDato(db,new Dato("Tipo de café", "Tomar Café", LocalDateTime.parse("2024-03-02T09:15:00"), "Latte"));
                usdbh. insertarDato(db,new Dato("Tazas", "Tomar Café", LocalDateTime.parse("2024-03-03T07:45:00"), "3"));
                usdbh. insertarDato(db,new Dato("Hora", "Tomar Café", LocalDateTime.parse("2024-03-03T07:45:00"), "2024-03-03T07:45:00"));
                usdbh. insertarDato(db,new Dato("Tipo de café", "Tomar Café", LocalDateTime.parse("2024-03-03T07:45:00"), "Americano"));

                // Datos para "Ir al gimnasio"
                usdbh. insertarDato(db,new Dato("Duración", "Ir al gimnasio", LocalDateTime.parse("2024-03-01T18:00:00"), "60"));
                usdbh. insertarDato(db,new Dato("Fecha", "Ir al gimnasio", LocalDateTime.parse("2024-03-01T18:00:00"), "2024-03-01"));
                usdbh. insertarDato(db,new Dato("Actividad", "Ir al gimnasio", LocalDateTime.parse("2024-03-01T18:00:00"), "Cardio"));
                usdbh. insertarDato(db,new Dato("Duración", "Ir al gimnasio", LocalDateTime.parse("2024-03-03T19:30:00"), "45"));
                usdbh. insertarDato(db,new Dato("Fecha", "Ir al gimnasio", LocalDateTime.parse("2024-03-03T19:30:00"), "2024-03-03"));
                usdbh. insertarDato(db,new Dato("Actividad", "Ir al gimnasio", LocalDateTime.parse("2024-03-03T19:30:00"), "Pesas"));
                usdbh. insertarDato(db,new Dato("Duración", "Ir al gimnasio", LocalDateTime.parse("2024-03-05T17:45:00"), "90"));
                usdbh. insertarDato(db,new Dato("Fecha", "Ir al gimnasio", LocalDateTime.parse("2024-03-05T17:45:00"), "2024-03-05"));
                usdbh. insertarDato(db,new Dato("Actividad", "Ir al gimnasio", LocalDateTime.parse("2024-03-05T17:45:00"), "Yoga"));

                // Datos para "Diario"
                usdbh. insertarDato(db,new Dato("Estado de ánimo", "Diario", LocalDateTime.parse("2024-03-01T22:00:00"), "Feliz"));
                usdbh. insertarDato(db,new Dato("Fecha", "Diario", LocalDateTime.parse("2024-03-01T22:00:00"), "2024-03-01"));
                usdbh. insertarDato(db,new Dato("Evento destacado", "Diario", LocalDateTime.parse("2024-03-01T22:00:00"), "Reunión con amigos"));
                usdbh. insertarDato(db,new Dato("Estado de ánimo", "Diario", LocalDateTime.parse("2024-03-02T21:30:00"), "Cansado"));
                usdbh. insertarDato(db,new Dato("Fecha", "Diario", LocalDateTime.parse("2024-03-02T21:30:00"), "2024-03-02"));
                usdbh. insertarDato(db,new Dato("Evento destacado", "Diario", LocalDateTime.parse("2024-03-02T21:30:00"), "Trabajo intenso"));
                usdbh. insertarDato(db,new Dato("Estado de ánimo", "Diario", LocalDateTime.parse("2024-03-03T23:00:00"), "Relajado"));
                usdbh. insertarDato(db,new Dato("Fecha", "Diario", LocalDateTime.parse("2024-03-03T23:00:00"), "2024-03-03"));
                usdbh. insertarDato(db,new Dato("Evento destacado", "Diario", LocalDateTime.parse("2024-03-03T23:00:00"), "Día de descanso"));

                // Marcar la transacción como exitosa
                db.setTransactionSuccessful();
            /*}catch (SQLiteException e){
                tvErrores.setError(e.getMessage());*/
            }catch (Exception e) {
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

    private long insertarEstudio(Estudio estudio){
        long  res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);) {

            SQLiteDatabase db = usdbh.getWritableDatabase();
            res = usdbh. insertarEstudio(db,estudio);
        }
        return res;
    }
    private int editarEstudio(Estudio antiguo, Estudio nuevo){
        int res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);){
            SQLiteDatabase db = usdbh.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("NOMBRE", nuevo.getNombre());
            values.put("DESCRIPCION", nuevo.getDescripcion());
            values.put("EMOJI", nuevo.getEmoji());

            //Actualizar usando el ID como condición
            String[] id = {antiguo.getNombre()};
            res= db.update("Estudio",
                    values,
                    "nombre = ?",
                    id);

            db.close();
        } catch (SQLiteConstraintException ex){
            toast(ex.getMessage());
        }
        return res;
    }
    private long insertarTipoDato(TipoDato tipoDato){
        long  res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);) {

            SQLiteDatabase db = usdbh.getWritableDatabase();
            res = usdbh. insertarTipoDato(db,tipoDato);
        }
        return res;
    }

    private boolean editarTipoDato(ArrayList<TipoDato> nuevoTiposDato){
        long[]  fun = new long[nuevoTiposDato.size()+1];
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);) {

            //Aquí tiene que llegar un tipo de Dato con FK obligatoriamente:
            SQLiteDatabase db = usdbh.getWritableDatabase();
            fun[0] = usdbh.borrarTiposDatos_PorFK(db, nuevoTiposDato.get(0).getFkEstudio());
            for (int i = 0; i < nuevoTiposDato.size(); i++) {
                //Apunta la clave Foránea del Estudio
                nuevoTiposDato.get(i).setFkEstudio(nuevoTiposDato.get(0).getFkEstudio());
                try{
                    fun[1+i] = usdbh.insertarTipoDato(db, nuevoTiposDato.get(i));
                }catch (SQLiteException e){
                    tvErrores.setText(e.getMessage());
                }
            }
            db.close();
        }
        return multiBoolean(fun);
    }

    private void rellenarLista(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from estudio", null);

        while (c.moveToNext()) {
            int index = c.getColumnIndex("NOMBRE");
            String nombre = c.getString(index);
            index = c.getColumnIndex("DESCRIPCION");
            String descripcion = c.getString(index);
            index = c.getColumnIndex("EMOJI");
            String emoji = c.getString(index);
            listaEstudios.add(new Estudio(nombre, descripcion, emoji));
        }
        c.close();
    }






    private long borrarSQL(Estudio libro) {
        long res;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);) {
            res = usdbh.borrarSQL(libro);

        }
        return res;
    }
    private void borrarTodo() {
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);) {
            usdbh.borrarSQL();
            //usdbh.borrarDatos();
            //usdbh.borrarOcurrencias();
            //usdbh.borrarTipo_Datos();

        }
    }


    /**
     * RECOGER ALTA ACTIVITY
     *
     *
     */
    ActivityResultLauncher<Intent>
            lanzadorAlta = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult resultado) {
                    if(resultado.getResultCode()==RESULT_OK) {

                        Intent data = resultado.getData();
                        assert data != null;
                        //RECOGER DATOS:
                        ArrayList<String> datosEstudio = data.getStringArrayListExtra("ESTUDIO");
                        ArrayList<TipoDato> tiposDato = data.getParcelableArrayListExtra("TIPOSDATO");

                        //INSERTAR EL ESTUDIO:
                        if(datosEstudio != null && tiposDato!=null){
                            Estudio nuevoEstudio = new Estudio(
                                    datosEstudio.get(0),
                                    datosEstudio.get(1),
                                    datosEstudio.get(2));
                            listaEstudios.add(nuevoEstudio);
                            if(insertarEstudio(nuevoEstudio)!=-1){
                                //INSERTAR LOS TIPOS DE DATO:
                                for (int i = 0; i < tiposDato.size(); i++) {
                                    //PONER LA FORÁNEA A LOS TIPOS DE DATO:
                                    TipoDato tipoNuevo = tiposDato.get(i);
                                    tipoNuevo.setFkEstudio(nuevoEstudio.getNombre());
                                    //INSERTAR
                                    insertarTipoDato(tiposDato.get(i));
                                }
                            }
                        }

                        actualizarDatos();
                    }else{
                        //SIN DATOS
                    }
                }
            });



    private int editarSQL(Estudio antiguo, Estudio nuevo){
        int res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);){
            SQLiteDatabase db = usdbh.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("NOMBRE", nuevo.getNombre());
            values.put("DESCRIPCION", nuevo.getDescripcion());

            // Actualizar usando el ID como condición
            String[] id = {antiguo.getNombre()};
            res= db.update("Estudio",
                    values,
                    "nombre = ?",
                    id);

            db.close();
        } catch (SQLiteConstraintException ex){
            toast(ex.getMessage());
        }
        return res;
    }


    /**
     * RECOGER EDIT ACTIVITY
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            adaptadorEstudios.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                    assert data != null;
                ArrayList<String> datosEstudio = data.getStringArrayListExtra("ESTUDIO");
                ArrayList<TipoDato> nuevosTiposDato = data.
                        getParcelableArrayListExtra("NUEVOSTIPOSDATO");

                int posicion = data.getIntExtra("INDEX", -1);

                //INSERTAR EL ESTUDIO:
                if (datosEstudio != null && nuevosTiposDato != null) {
                    Estudio editEstudio = new Estudio(
                            datosEstudio.get(0),
                            datosEstudio.get(1),
                            datosEstudio.get(2));
                    Estudio viejo = listaEstudios.get(posicion);
                    nuevosTiposDato.get(0).setFkEstudio(viejo.getNombre());
                    if (editarEstudio(viejo, editEstudio) != -1) {
                        //INSERTAR LOS TIPOS DE DATO:
                        editarTipoDato(nuevosTiposDato);
                    }
                }
            }
        } else if (requestCode == 2) {
            //RECOGER BORRAR DE ADAPTADORESTUDIOS

            if (resultCode == RESULT_OK) {
                assert data != null;
                Estudio estudio = data.getParcelableExtra("ESTUDIO");
                int posicion = data.getIntExtra("INDEX", -1);

                listaEstudios.remove(estudio);


            }

        }
        actualizarDatos();



    /*
    //RECOGER EDIT ACTIVITY
    ActivityResultLauncher<Intent> lanzadorEdit = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult resultado)
                {
                    if(resultado.getResultCode()==RESULT_OK) {
                        Intent data = resultado.getData();
                        assert data != null;
                        ArrayList<String> datosEstudio = data.getStringArrayListExtra("ESTUDIO");
                        ArrayList<TipoDato> tiposDato = data.getParcelableArrayListExtra("TIPOSDATO");
                        int posicion = data.getIntExtra("INDEX",-1);

                        //INSERTAR EL ESTUDIO:
                        if(datosEstudio != null && tiposDato!=null){
                            Estudio editEstudio = new Estudio(
                                    datosEstudio.get(0),
                                    datosEstudio.get(1),
                                    datosEstudio.get(2));
                            Estudio viejo = listaEstudios.get(posicion);
                            if(insertarEstudio(editEstudio)!=-1){
                                //INSERTAR LOS TIPOS DE DATO:
                                for (int i = 0; i < tiposDato.size(); i++) {
                                    //PONER LA FORÁNEA A LOS TIPOS DE DATO:
                                    TipoDato tipoNuevo = tiposDato.get(i);
                                    tipoNuevo.setFkEstudio(editEstudio.getNombre());
                                    //INSERTAR
                                    insertarTipoDato(tiposDato.get(i));
                                }
                            }
                        }

                        actualizarDatos();

                    }else{
                        //SIN DATOS
                    }
                }
            }
    );*/


    }

}