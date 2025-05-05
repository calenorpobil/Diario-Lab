package com.merlita.diariolab;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;
import static com.merlita.diariolab.Utils.Utils.copiarArchivo;
import static com.merlita.diariolab.Utils.Utils.multiBoolean;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorEstudios;
import com.merlita.diariolab.Modelos.Cualitativo;
import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;
import com.merlita.diariolab.Utils.FileHelper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static RecyclerView vistaRecycler;
    public static ArrayList<Estudio> listaEstudios = new ArrayList<>();
    private ArrayList<Integer> cuenta = new ArrayList<>();
    TextView tv, tvErrores;
    static AdaptadorEstudios adaptadorEstudios;
    Button btAlta, btDev, btRevert;



    public final static int DB_VERSION=14;



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


//        Intent i = new Intent(MainActivity.this, AnalisisActivity.class);
//        lanzadorAlta.launch(i);

        tv = findViewById(R.id.tvEstudio2);
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


    private ArrayList<Integer> getOcurrencia(ArrayList<Estudio> estudios) {
        ArrayList<Integer> res = new ArrayList<>();


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null,  DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            for (int i = 0; i < estudios.size(); i++) {
                res.add(usdbh.getCuentaOcurrencias(db, estudios.get(i).getNombre()));
            }

            db.close();
        }

        return res;
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
                cuenta = getOcurrencia(listaEstudios);
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

            //usdbh.onCreate(db);

            //Iniciar una transacción para mejorar el rendimiento
            try {
                ArrayList<Estudio> estudios = new ArrayList<>();
                estudios.add(new Estudio("Tomar Café", "Registro de consumo diario de café", "☕", 0));
                estudios.add(new Estudio("Ir al gimnasio", "Seguimiento de sesiones de entrenamiento", "\uD83C\uDFCB\uFE0F", 0));
                estudios.add(new Estudio("Diario", "Registro personal diario", "\uD83D\uDCD4", 0));
                ArrayList<TipoDato>[] a = new ArrayList[estudios.size()];
                ArrayList<TipoDato> tiposDato1 = new ArrayList<>();

                Ocurrencia aux;
                int esteEstudio=0, numOcurrencia=-1;
                if(!usdbh.estudioExiste(db, "Tomar Café")){
                    // Insertar tipos de datos para "Tomar Café"
                    tiposDato1.add(new TipoDato("Tazas", "Número", "Cantidad de tazas consumidas", "Tomar Café"));
                    tiposDato1.add(new TipoDato("Hora", "Fecha", "Hora en que se tomó el café", "Tomar Café"));
                    tiposDato1.add(new TipoDato("Tipo de café", "Texto", "Tipo de café consumido", "Tomar Café"));
                    a[0] = tiposDato1;

                    usdbh.insertarEstudio(db, estudios.get(esteEstudio));
                    ArrayList<TipoDato> tiposAux = a[esteEstudio];
                    for (int j = 0; j < tiposAux.size(); j++) {
                        usdbh.insertarTipoDato(db, tiposAux.get(j));
                    }

                    // Insertar ocurrencias para "Tomar Café"
                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-01"), "Tomar Café");
                    usdbh. insertarOcurrencia(db,aux);
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(0), "Tomar Café", aux.getCod(), "2"));
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(1), "Tomar Café", aux.getCod(), "2024-03-01T08:30:00"));
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(2), "Tomar Café", aux.getCod(), "Espresso"));

                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-02"), "Tomar Café");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(0), "Tomar Café", aux.getCod(), "1"));
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(1), "Tomar Café", aux.getCod(), "2024-03-02T09:15:00"));
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(2), "Tomar Café", aux.getCod(), "Latte"));

                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-03"), "Tomar Café");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(0), "Tomar Café", aux.getCod(), "3"));
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(1), "Tomar Café", aux.getCod(), "2024-03-03"));
                    usdbh. insertarDato(db, new Dato(tiposDato1.get(2), "Tomar Café", aux.getCod(), "Americano"));

                }

                esteEstudio=1;
                numOcurrencia=-1;
                if(!usdbh.estudioExiste(db, "Ir al gimnasio")){
                    // Tipos para "Ir al gimnasio"
                    ArrayList<TipoDato> tiposDato2 = new ArrayList<>();
                    tiposDato2.add(new TipoDato("Duración", "Número", "Duración del entrenamiento en minutos", "Ir al gimnasio"));
                    tiposDato2.add(new TipoDato("Fecha", "Fecha", "Fecha del entrenamiento", "Ir al gimnasio"));
                    tiposDato2.add(new TipoDato("Actividad", "Texto", "Tipo de actividad realizada", "Ir al gimnasio"));
                    a[1] = tiposDato2;

                    usdbh.insertarEstudio(db, estudios.get(esteEstudio));
                    ArrayList<TipoDato> tiposAux = a[esteEstudio];
                    for (int j = 0; j < tiposAux.size(); j++) {
                        usdbh.insertarTipoDato(db, tiposAux.get(j));
                    }
                    // Ocurrencias para "Ir al gimnasio"
                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-01"), "Ir al gimnasio");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(0), "Ir al gimnasio", aux.getCod(), "60"));
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(1), "Ir al gimnasio", aux.getCod(), "2024-03-01"));
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(2), "Ir al gimnasio", aux.getCod(), "Cardio"));

                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-03"), "Ir al gimnasio");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(0), "Ir al gimnasio", aux.getCod(), "45"));
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(1), "Ir al gimnasio", aux.getCod(), "2024-03-03"));
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(2), "Ir al gimnasio", aux.getCod(), "Pesas"));

                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-05"), "Ir al gimnasio");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(0), "Ir al gimnasio", aux.getCod(), "90"));
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(1), "Ir al gimnasio", aux.getCod(), "2024-03-05"));
                    usdbh. insertarDato(db,new Dato(tiposDato2.get(2), "Ir al gimnasio", aux.getCod(), "Yoga"));
                }

                esteEstudio=2;
                numOcurrencia=-1;
                if(!usdbh.estudioExiste(db, "Diario")){
                    // Tipos para "Diario"
                    ArrayList<TipoDato> tiposDato3 = new ArrayList<>();
                    tiposDato3.add(new TipoDato("Estado de ánimo", "Texto", "Descripción del estado de ánimo", "Diario"));
                    tiposDato3.add(new TipoDato("Fecha", "Fecha", "Fecha del registro", "Diario"));
                    tiposDato3.add(new TipoDato("Evento destacado", "Texto", "Evento importante del día", "Diario"));
                    a[2] = tiposDato3;

                    usdbh.insertarEstudio(db, estudios.get(2));
                    ArrayList<TipoDato> tiposAux = a[2];
                    for (int j = 0; j < tiposAux.size(); j++) {
                        usdbh.insertarTipoDato(db, tiposAux.get(j));
                    }

                    // Ocurrencias para "Diario"
                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-01"), "Diario");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(0), "Diario", aux.getCod(), "Feliz"));
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(1), "Diario", aux.getCod(), "2024-03-01"));
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(2), "Diario", aux.getCod(), "Reunión con amigos"));

                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-02"), "Diario");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(0), "Diario", aux.getCod(), "Cansado"));
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(1), "Diario", aux.getCod(), "2024-03-02"));
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(2), "Diario", aux.getCod(), "Trabajo intenso"));

                    numOcurrencia = usdbh.getCuentaOcurrencias(db, estudios.get(esteEstudio).getNombre());
                    aux = new Ocurrencia(numOcurrencia, LocalDate.parse("2024-03-03"), "Diario");
                    usdbh. insertarOcurrencia(db, aux);
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(0), "Diario", aux.getCod(), "Relajado"));
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(1), "Diario", aux.getCod(), "2024-03-03"));
                    usdbh. insertarDato(db,new Dato(tiposDato3.get(2), "Diario", aux.getCod(), "Día de descanso"));
                }

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

    //Añade repetición al estudio:
    private int addRepsEstudio(Estudio antiguo){
        int res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);){
            SQLiteDatabase db = usdbh.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("REPS", antiguo.getReps()+1);

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
    private long[] insertarDatos(ArrayList<Dato> datos){
        long[] res = new long[datos.size()];
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
                if(valor.equals("")){
                    values.put("VALOR_TEXT", " ");
                }else{
                    values.put("VALOR_TEXT", valor);
                }

                res[i]= db.insert("Dato", null,
                        values);

            }

            db.close();
        } catch (SQLiteConstraintException ex){
            toast(ex.getMessage());
        } catch (SQLiteException ex){
            toast(ex.getMessage());
        }
        return res;
    }
    private long[] borrarDatos(ArrayList<Dato> datos){
        long[] res = new long[datos.size()];
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);){
            SQLiteDatabase db = usdbh.getWritableDatabase();

            for (int i = 0; i < datos.size(); i++) {

                res[i]= db.delete("Dato",
                        "fk_ocurrencia = ?",
                        new String[]{datos.get(0).getFkOcurrencia()});

            }

            db.close();
        } catch (SQLiteConstraintException ex){
            toast(ex.getMessage());
        }
        return res;
    }
    private long insertarOcurrencia(Ocurrencia ocurrencia) throws SQLiteException {
        long res = 0;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, DB_VERSION);){
            SQLiteDatabase db = usdbh.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("FECHA", ocurrencia.getFecha().toString());
            values.put("FK_ESTUDIO_N", ocurrencia.getFkEstudioN());
            values.put("ID", ocurrencia.getCod());

            res = db.insertOrThrow("Ocurrencia", null,
                    values);


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
            res = usdbh.insertarTipoDato(db,tipoDato);
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
//            usdbh.borrarDatos();
//            usdbh.borrarEstudios();
//            usdbh.borrarOcurrencias();
//            usdbh.borrarTipo_Datos();
//            usdbh.borrarCualitativos();

            SQLiteDatabase db = usdbh.getWritableDatabase();

            usdbh.onCreate(db);

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
                        // RECOGER DATOS:
                        ArrayList<String> datosEstudio = data.getStringArrayListExtra("ESTUDIO");
                        ArrayList<TipoDato> tiposDato = data.getParcelableArrayListExtra("TIPOSDATO");
                        ArrayList<Cualitativo> cualitativos = data.getParcelableArrayListExtra("CUALITATIVOS");

                        // INSERTAR EL ESTUDIO:
                        if(datosEstudio != null && tiposDato!=null){
                            Estudio nuevoEstudio = new Estudio(
                                    datosEstudio.get(0),
                                    datosEstudio.get(1),
                                    datosEstudio.get(2));
                            listaEstudios.add(nuevoEstudio);
                            if(insertarEstudio(nuevoEstudio)!=-1){
                                // INSERTAR LOS TIPOS DE DATO:
                                for (int i = 0; i < tiposDato.size(); i++) {
                                    // PONER LA FORÁNEA A LOS TIPOS DE DATO:
                                    TipoDato tipoNuevo = tiposDato.get(i);
                                    tipoNuevo.setFkEstudio(nuevoEstudio.getNombre());
                                    for (int j = 0; j < cualitativos.size(); j++) {
                                        Cualitativo check = cualitativos.get(j);
                                        if(check.
                                                getFk_dato_tipo_t().equals(tipoNuevo.getNombre())){
                                            tipoNuevo.setCualitativo(check);
                                        }
                                    }
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
     * RECOGER ACTIVITIES DEL RECYCLER VIEW
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //RECOGER EDIT ACTIVITY
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
        //RECOGER BORRAR DE ADAPTADORESTUDIOS
        } else if (requestCode == 2) {

            if (resultCode == RESULT_OK) {
                assert data != null;
                Estudio estudio = data.getParcelableExtra("ESTUDIO");
                int posicion = data.getIntExtra("INDEX", -1);

                listaEstudios.remove(estudio);


            }
        //RECOGER NUEVA OCURRENCIA
        } else if (requestCode == 3) {

            if (resultCode == RESULT_OK) {
                assert data != null;
                Ocurrencia datosOcurrencia = data.getParcelableExtra("OCURRENCIA");
                Estudio datosEstudio = data.getParcelableExtra("ESTUDIO");
                ArrayList<Dato> nuevosDatos = data.
                        getParcelableArrayListExtra("DATOS");

                assert datosEstudio != null;
                int reps = datosEstudio.getReps()+1;
                datosEstudio.setReps(reps);

                if (nuevosDatos != null) {
                    nuevosDatos.get(0).setFkOcurrencia(datosOcurrencia.getCod());
                    long[] res = insertarDatos(nuevosDatos);
                    boolean algoFallo = false;
                    for (int i = 0; i < res.length; i++) {
                        if(res[i]==0 || res[i]==-1){
                            algoFallo = true;
                        }
                    }
                    if(!algoFallo){
                        long resOcurrencia = insertarOcurrencia(datosOcurrencia);
                        addRepsEstudio(datosEstudio);
                    } else {
                        borrarDatos(nuevosDatos);
                    }
                }
            }
        }
        actualizarDatos();
    }
}