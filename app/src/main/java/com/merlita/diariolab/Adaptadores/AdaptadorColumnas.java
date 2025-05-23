package com.merlita.diariolab.Adaptadores;

import static com.merlita.diariolab.MainActivity.DB_VERSION;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.R;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;

public class AdaptadorColumnas extends RecyclerView.Adapter<AdaptadorColumnas.MiContenedor> {



    private Context context;
    private ArrayList<Ocurrencia> listaOcurrencias;
    private ArrayList<Dato> listaDatos;
    private ArrayList<TipoDato> listaTipos;
    private AdaptadorColumnas.OnButtonClickListener listener;


    public AdaptadorColumnas(Context context,
                             ArrayList<Ocurrencia> listaOcurrencias, ArrayList<Dato> listaDatos,
                             ArrayList<TipoDato> listaTipos, OnButtonClickListener listener){
        this.context = context;
        this.listaOcurrencias = listaOcurrencias;
        this.listaDatos = listaDatos;
        this.listaTipos = listaTipos;
        this.listener = listener;
    }

    public static class MiContenedor extends RecyclerView.ViewHolder
    {
        View columnaDato;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            columnaDato = (View) itemView.findViewById(R.id.vwPunto);
        }


    }

    @NonNull
    @Override
    public AdaptadorColumnas.MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflador =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflador.inflate(R.layout.columna_grafico, parent, false);


        return new AdaptadorColumnas.MiContenedor(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorColumnas.MiContenedor holder, int position) {
        Ocurrencia ocurrencia = listaOcurrencias.get(holder.getAbsoluteAdapterPosition());
        ArrayList<Dato> listaDatosUtiles = new ArrayList<>();

        //Solo mostrará la longitud de los datos de texto de momento:
        Double max = 0.0;
        Double longitud = 0.0;

        Dato dato = getPrimerDatoOcurrencia(ocurrencia, ocurrencia.getFkEstudioN());
        if (dato!=null) {
            int index = listaTipos.indexOf(
                    new TipoDato(dato.getFkTipoDato(), dato.getFkTipoEstudio()));
            TipoDato tipoCorrespondiente = listaTipos.get(index);
            if(tipoCorrespondiente.getTipoDato().equals("Texto")){
                listaDatosUtiles.add(dato);
            }
            if(!listaDatosUtiles.isEmpty()){
                String texto = listaDatosUtiles.get(0).getValorText();
                longitud = (double) texto.length();
                max = (double) tipoCorrespondiente.getMaximaLongitud();
            }
        }


        //}

        View columna = holder.columnaDato;

        double porcentaje;
        if(longitud !=0 && max !=0)
            porcentaje = longitud/max;
        else {
            porcentaje = 1;
        }

        ViewGroup.LayoutParams params = columna.getLayoutParams();
        int height = columna.getHeight();
        int res = (int) (Math.round(height*porcentaje));
        columna.setLayoutParams(params);

        columna.post(() -> {
            int alturaActual = columna.getHeight();

            int alturaNueva = (int) (Math.floor(alturaActual *porcentaje));
            int margenNuevo = (int) (Math.floor(alturaActual *(1-porcentaje)));
            ViewGroup.LayoutParams existingLayoutParams = columna.getLayoutParams();
            LinearLayout.LayoutParams newLayoutParams = new LinearLayout.LayoutParams(
                    existingLayoutParams.width,
                    alturaNueva);
            newLayoutParams.height = alturaNueva;
            newLayoutParams.topMargin = margenNuevo;
            newLayoutParams.leftMargin = 16;
            columna.setLayoutParams(newLayoutParams);
        });

        holder.columnaDato.post(new Runnable() {
            @Override
            public void run() {

            }
        });

        holder.columnaDato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClickTipo();
            }
        });

    }

    private Dato getPrimerDatoOcurrencia(Ocurrencia ocurrencia, String nombreEstudio) {
        Dato datoResultado=null;
        ArrayList<Dato> datos;

        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this.context,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                datos = usdbh.getDatosPorOcurrencia(db, ocurrencia.getCod(), nombreEstudio);
                if(!datos.isEmpty())
                    datoResultado = datos.get(0);

                db.close();
            }
        } catch (SQLiteDatabaseCorruptException ex){
            Log.d("MyAdapter", "Intentalo en otro momento. ");
        }
        return datoResultado;

    }

    @Override
    public int getItemCount() {
        int size = 0;
        if(listaOcurrencias!=null){
            size = listaOcurrencias.size();
        }
        return size;
    }


    public interface OnButtonClickListener {
        void onButtonClickTipo();
    }



}
