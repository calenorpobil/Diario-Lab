package com.merlita.diariolab;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;
import static com.merlita.diariolab.MainActivity.DB_VERSION;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorAnalisis;
import com.merlita.diariolab.Modelos.Analisis;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalisisActivity extends AppCompatActivity {

    static RecyclerView rvAnalisis;
    public static ArrayList<Analisis> listaAnalisis = new ArrayList<>();
    private AdaptadorAnalisis adaptadorAnalisis;
    Button btHome;
    String nombreEstudio;


    private Uri selectedFileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analisis);

        Bundle upIntent = this.getIntent().getExtras();
        assert upIntent != null;

        nombreEstudio = upIntent.getString("ESTUDIO");

        btHome = findViewById(R.id.btHome);
        rvAnalisis = findViewById(R.id.rvAnalisis);
        ArrayList<TipoDato> tiposEstudio = getTiposEstudio();
        List<List<TipoDato>> pairs = new ArrayList<>();
        for (int i = 0; i < tiposEstudio.size(); i++) {
            for (int j = i + 1; j < tiposEstudio.size(); j++) {
                pairs.add(Arrays.asList(
                        tiposEstudio.get(i), tiposEstudio.get(j)));
                listaAnalisis.add(new Analisis(this,
                        nombreEstudio, nombreEstudio,
                        tiposEstudio.get(i).getNombre(), tiposEstudio.get(j).getNombre()));
            }
        }
//        listaAnalisis.add(new Analisis(this,
//                nombreEstudio,
//                nombreEstudio,
//                "Hora",
//                "Tipo de café"));
        adaptadorAnalisis = new AdaptadorAnalisis(this, this,
                listaAnalisis);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(VERTICAL);
        lm.setReverseLayout(true);

        rvAnalisis.setLayoutManager(lm);
        rvAnalisis.setAdapter(adaptadorAnalisis);
        actualizarDatos();

        btHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            }
        });
    }

    private ArrayList<TipoDato> getTiposEstudio() {
        ArrayList<TipoDato> res = new ArrayList<>();


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this,
                            "DBEstudios", null,  DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getTiposDato(db, nombreEstudio);

            db.close();
        }

        return res;
    }

    public static void actualizarLocal(){
        rvAnalisis.setAdapter(MainActivity.adaptadorEstudios);

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

                //listaAnalisis.clear();

                rellenarLista(db);
                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
        }
        rvAnalisis.setLayoutManager(new LinearLayoutManager(this));
        rvAnalisis.setAdapter(adaptadorAnalisis);

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
            //listaAnalisis.add(new Estudio(nombre, descripcion, emoji));
        }
        c.close();
    }


}