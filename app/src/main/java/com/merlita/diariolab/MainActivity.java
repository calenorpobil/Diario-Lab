package com.merlita.diariolab;

import static com.merlita.diariolab.Utils.copiarArchivo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import com.merlita.diariolab.Modelos.DatoTipo;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        AdaptadorFilas.OnButtonClickListener{

    RecyclerView vistaRecycler;
    ArrayList<Estudio> listaEstudios = new ArrayList<Estudio>();
    TextView tv;
    AdaptadorFilas adaptadorFilas;
    Button btAlta, btCopia, btRevert;
    EditText et;
    int posicionEdicion;
    boolean ver=true;
    boolean isRecibiendo =false, isEnviando = false;
    int numServidor=1;

    ResultCallbackEnviar callbackEnviarServer;
    ResultCallbackRecibir callbackRecibirServer;


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
        btCopia = findViewById(R.id.btCopia);
        btRevert = findViewById(R.id.btRevert);
        vistaRecycler = findViewById(R.id.recyclerView);
        adaptadorFilas = new AdaptadorFilas(this, listaEstudios, this);


        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorFilas);

        borrarTodo();
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


                /*try {
                    recibirArchivo();
                }catch (SQLiteDatabaseCorruptException ex) {
                    toast("El archivo está en uso. Inténtalo más tarde. ");
                }*/




            }
        });

        btCopia.setOnClickListener(new View.OnClickListener() {
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

    public void actualizarDatos() {
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this,
                                "DBEstudios", null, 1);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                listaEstudios.clear();

                rellenarLista(db);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            toast("Intentalo en otro momento. ");
        }
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorFilas);

    }


    private void recibirArchivo() {

        new Thread(recibirServer).start();

        // Comprobar Resultado
        callbackRecibirServer = new ResultCallbackRecibir() {
            @Override
            public void onSuccess() {
                // Actualizar UI o lógica post-éxito
                runOnUiThread(() -> {
                    toast("Se descargaron los datos de la nube. ");
                    try {
                        copiarArchivo(database_server, database);
                        toast("Backup de la nube revertida. ");
                        actualizarDatos();
                    } catch (IOException e) {
                        toast("No hay un archivo para revertir. ");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Manejar error
                runOnUiThread(() -> {
                    try {
                        //REVERTIR LOCALMENTE
                        copiarArchivo(bk_database, database);
                        toast("Backup revertida localmente. ");
                        actualizarDatos();
                    } catch (IOException ex) {
                        toast("No se pudo revertir la copia. ");
                    }
                });
            }
        };

    }

    Runnable recibirServer = new Runnable() {
        public void run() {
            File archivoDestino;
            try (Socket socket = new Socket();) {
                socket.connect(new InetSocketAddress(SERVIDOR_IP, PUERTO), 1000);
                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());

                //DIGO AL SERVER QUE QUIERO RECIBIR ARCHIVO
                outStream.writeUTF("RECIBIR");

                System.out.println("Recibir dicho. ");

                // Solicitar archivo "DBEstudios"
                outStream.writeUTF("DBEstudios");


                try (DataInputStream inStream = new DataInputStream(socket.getInputStream())) {
                    // Recibir metadatos
                    String nombreArchivo = inStream.readUTF();
                    long tamanyoArchivo = inStream.readLong();

                    // Prepara el archivo de destino (cliente)
                    archivoDestino = database_server;

                    archivoDestino.delete();

                    if (!archivoDestino.exists()) {
                        try {
                            archivoDestino.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //GUARDAR ARCHIVO
                    try (FileOutputStream fos = new FileOutputStream(database_server);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {


                        // Recibir y guardar el archivo por bloques de 4KB
                        byte[] buffer = new byte[4096];
                        int count;
                        long totalRecibido = 0;
                        while (totalRecibido < tamanyoArchivo && (count = inStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, count);
                            totalRecibido += count;
                        }

                        System.out.println("Archivo recibido: " + nombreArchivo);
                        // Notificar éxito
                        if (callbackRecibirServer != null) {
                            new Handler(Looper.getMainLooper()).post(() -> callbackRecibirServer.onSuccess());
                        }
                    }
                }
            } catch (IOException e) {

                new Handler(Looper.getMainLooper()).post(() -> callbackRecibirServer.onFailure(e));
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    };
    private void enviarArchivoNube() {
        //ARCHIVO SQLITE:


        new Thread(enviarANube).start();

        // Comprobar Resultado
        callbackEnviarServer = new ResultCallbackEnviar() {
            @Override
            public void onSuccess() {
                // Actualizar UI o lógica post-éxito
                runOnUiThread(() -> {
                    toast("Se guardó el mensaje en la nube. ");
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Manejar error
                runOnUiThread(() -> {
                    toast("La copia al servidor no funcionó. Se hará una backup local. ");
                    backupLocalCopiar();
                });
            }
        };



    }


    // Request code for creating a PDF document.
    private static final int CREATE_FILE = 1;


    public interface ResultCallbackEnviar {
        void onSuccess();
        void onFailure(Exception e);
    }
    public interface ResultCallbackRecibir {
        void onSuccess();
        void onFailure(Exception e);
    }

    Runnable enviarANube = new Runnable() {
        @Override
        public void run() {
            try {
                final int BUFFER_SIZE = 4096; // 4 KB

                try (Socket socket = new Socket()) {

                    FileInputStream fis = new FileInputStream(database);
                    BufferedInputStream inStream = new BufferedInputStream(fis);
                    socket.connect(new InetSocketAddress(SERVIDOR_IP, PUERTO), 1000);
                    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                    //ENVIAR ARCHIVO
                    outStream.writeUTF("ENVIAR");


                    // Enviar metadatos: nombre y tamaño
                    outStream.writeUTF(database.getName()); // Nombre del archivo
                    outStream.writeLong(database.length()); // Tamaño en bytes

                    // Enviar archivo
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int count;
                    while ((count = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, count);
                    }

                    System.out.println("Archivo enviado: " + database.getName());
                    // Notificar éxito
                    if (callbackEnviarServer != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callbackEnviarServer.onSuccess());
                    }


                } catch(SocketException ex) {
                    if (callbackEnviarServer != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callbackEnviarServer.onFailure(ex));
                    }
                }catch (UnknownHostException e) {
                    System.err.println("Host desconocido: " + SERVIDOR_IP);
                    if (callbackEnviarServer != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callbackEnviarServer.onFailure(e));
                    }
                } catch (IOException e) {
                    System.err.println("Error de E/S: " + e.getMessage());
                    if (callbackEnviarServer != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callbackEnviarServer.onFailure(e));
                    }
                }
            } catch (Exception e) {
                //NOTIFICAR ERROR
                if (callbackEnviarServer != null) {
                    new Handler(Looper.getMainLooper()).post(() -> callbackEnviarServer.onFailure(e));
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    };

    private void mostrarFormularioAlta()  {
        Intent i = new Intent(MainActivity.this, AltaActivity.class);
        lanzadorAlta.launch(i);
    }



    public void insertarDatosIniciales() {
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, 1);) {


            SQLiteDatabase db = usdbh.getWritableDatabase();

            // Iniciar una transacción para mejorar el rendimiento
            try {
                db.beginTransaction();
                // Insertar estudios
                usdbh.insertarEstudio(db, new Estudio("Tomar Café", "Registro de consumo diario de café"));
                usdbh. insertarEstudio(db,new Estudio("Ir al gimnasio", "Seguimiento de sesiones de entrenamiento"));
                usdbh. insertarEstudio(db,new Estudio("Diario", "Registro personal diario"));

                // Insertar tipos de datos para "Tomar Café"
                usdbh. insertarDatoTipo(db,new DatoTipo("Tazas", "Numero", "Cantidad de tazas consumidas", "Tomar Café"));
                usdbh. insertarDatoTipo(db,new DatoTipo("Hora", "Fecha", "Hora en que se tomó el café", "Tomar Café"));
                usdbh. insertarDatoTipo(db,new DatoTipo("Tipo de café", "Texto", "Tipo de café consumido", "Tomar Café"));

                // Tipos para "Ir al gimnasio"
                usdbh. insertarDatoTipo(db,new DatoTipo("Duración", "Numero", "Duración del entrenamiento en minutos", "Ir al gimnasio"));
                usdbh. insertarDatoTipo(db,new DatoTipo("Fecha", "Fecha", "Fecha del entrenamiento", "Ir al gimnasio"));
                usdbh. insertarDatoTipo(db,new DatoTipo("Actividad", "Texto", "Tipo de actividad realizada", "Ir al gimnasio"));

                // Tipos para "Diario"
                usdbh. insertarDatoTipo(db,new DatoTipo("Estado de ánimo", "Texto", "Descripción del estado de ánimo", "Diario"));
                usdbh. insertarDatoTipo(db,new DatoTipo("Fecha", "Fecha", "Fecha del registro", "Diario"));
                usdbh. insertarDatoTipo(db,new DatoTipo("Evento destacado", "Texto", "Evento importante del día", "Diario"));

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

    private long insertarSQL(Estudio estudio){
        long  res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, 1);) {

            SQLiteDatabase db = usdbh.getWritableDatabase();
            res = usdbh. insertarEstudio(db,estudio);
        }
        return res;
    }

    private void rellenarLista(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from estudio", null);

        while (c.moveToNext()) {
            int index = c.getColumnIndex("NOMBRE");
            String nombre = c.getString(index);
            index = c.getColumnIndex("DESCRIPCION");
            String descripcion = c.getString(index);
            listaEstudios.add(new Estudio(nombre, descripcion));
        }
        c.close();
    }





    //MENU CONTEXTUAL
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item){
        Estudio libro;
        switch(item.getItemId())
        {
            case 121:
                //MENU --> EDITAR
                Intent i = new Intent(this, EditActivity.class);
                posicionEdicion = item.getGroupId();
                libro = listaEstudios.get(posicionEdicion);
                i.putExtra("NOMBRE", libro.getNombre());
                i.putExtra("DESCRIPCION", libro.getDescripcion());
                lanzadorEdit.launch(i);
                return true;
            case 122:
                //MENU --> BORRAR
                posicionEdicion = item.getGroupId();
                libro = listaEstudios.get(posicionEdicion);
                if(borrarSQL(libro)!=-1){
                    listaEstudios.remove(libro);
                }
                actualizarDatos();

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private long borrarSQL(Estudio libro) {
        long res;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, 1);) {
            res = usdbh.borrarSQL(libro);

        }
        return res;
    }
    private void borrarTodo() {
        long res;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, 1);) {
            res = usdbh.borrarTodo();

        }
    }


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
                        Estudio editLibro = new Estudio(
                                data.getStringExtra("NOMBRE"),
                                data.getStringExtra("DESCRIPCION")
                        );

                        Estudio antig = listaEstudios.get(posicionEdicion);
                        // Editar el libro

                        int insertado = editarSQL(antig, editLibro);
                        if(insertado != -1){
                            listaEstudios.set(listaEstudios.indexOf(antig), editLibro);
                        }

                        actualizarDatos();

                    }else{
                        //SIN DATOS
                    }
                }
            }
    );


    //RECOGER ALTA ACTIVITY
    ActivityResultLauncher<Intent>
            lanzadorAlta = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult resultado) {
                    if(resultado.getResultCode()==RESULT_OK) {

                        Intent data = resultado.getData();
                        assert data != null;
                        String nombre = data.getStringExtra("NOMBRE");
                        String desc = data.getStringExtra("DESCRIPCION");
                        Estudio nuevoEstudio = new Estudio(nombre, desc);

                        // Insertar en BD
                        long fila = insertarSQL(nuevoEstudio);
                        if(fila!=-1){
                            System.out.println(fila);
                            listaEstudios.add(nuevoEstudio);
                        }
                        actualizarDatos();
                    }else{
                        //SIN DATOS
                    }
                }
            });


    //RECOGER ACERCA DE
    ActivityResultLauncher<Intent>
            lanzadorAcercaDe = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult resultado) {
                    if(resultado.getResultCode()==RESULT_OK) {

                        Intent data = resultado.getData();
                        assert data != null;

                    }else{
                        //SIN DATOS
                    }
                }
            });




    private int editarSQL(Estudio antiguo, Estudio nuevo){
        int res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null, 1);){
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



    @Override
    public void onButtonClick(int position) {
        // Lógica de actualización (ejemplo: modificar el elemento)
        actualizarDatos();
    }



}