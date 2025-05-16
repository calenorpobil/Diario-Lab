package com.merlita.diariolab;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;
import static com.merlita.diariolab.MainActivity.DB_VERSION;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorEstudiosElegir;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;

public class ElegirActivity extends AppCompatActivity {

    static RecyclerView vistaRecycler;
    public static ArrayList<Estudio> listaEstudios = new ArrayList<>();
    static AdaptadorEstudiosElegir adaptadorEstudios;
    Button btAnalizar;
    String nombreEstudio;

    TextView tvEstudio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir);

        Bundle upIntent = this.getIntent().getExtras();
        assert upIntent != null;

        nombreEstudio = upIntent.getString("ESTUDIO");

        btAnalizar = findViewById(R.id.btHome);
        vistaRecycler = findViewById(R.id.rvAnalisis);
        tvEstudio = findViewById(R.id.tvEstudioElegir);
        adaptadorEstudios = new AdaptadorEstudiosElegir(this, listaEstudios);



        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(VERTICAL);
        lm.setReverseLayout(true);

        vistaRecycler.setLayoutManager(lm);
        vistaRecycler.setAdapter(adaptadorEstudios);
        actualizarDatos();

        btAnalizar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent i = new Intent(ElegirActivity.this, AnalisisActivity.class);
                i.putExtra("ESTUDIO", nombreEstudio);

                lanzadorAlta.launch(i);

            }
        });
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
                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
        }
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));
        vistaRecycler.setAdapter(adaptadorEstudios);

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


}