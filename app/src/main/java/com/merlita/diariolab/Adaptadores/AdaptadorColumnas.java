package com.merlita.diariolab.Adaptadores;

import static android.view.View.INVISIBLE;
import static com.merlita.diariolab.MainActivity.DB_VERSION;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.R;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.time.LocalDate;
import java.util.ArrayList;

public class AdaptadorColumnas extends RecyclerView.Adapter<AdaptadorColumnas.MiContenedor>
        implements AdaptadorTiposGrafico.OnButtonClickListener{



    private final Context context;
    private ArrayList<Dato> listaDatos;
    private TipoDato tipoActual;


    public AdaptadorColumnas(Context context, ArrayList<Dato> listaDatos,
                             TipoDato tipo){
        this.context = context;
        this.listaDatos = listaDatos;
        this.tipoActual = tipo;
    }

    public static class MiContenedor extends RecyclerView.ViewHolder
    {
        TextView tvPorcentaje;
        TextView tvOcu;
        TextView tvDato, tvCalculo;
        ConstraintLayout cl;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            tvPorcentaje = (TextView) itemView.findViewById(R.id.tvPorcentaje);
            tvOcu = (TextView) itemView.findViewById(R.id.tvFechaOcu);
            tvDato = (TextView) itemView.findViewById(R.id.tvDato);
            tvCalculo = (TextView) itemView.findViewById(R.id.tvCalculo);

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
        ArrayList<Dato> listaDatosUtiles = new ArrayList<>();
        TextView tvPorcentaje = holder.tvPorcentaje;
        Dato dato = listaDatos.get(holder.getAbsoluteAdapterPosition());
        String fechaDato = getFechaDato(dato);
        holder.tvOcu.setText(fechaDato);
        holder.tvDato.setText(dato.getValorText());

        //Solo mostrará la longitud de los datos de texto de momento:
        double max = 0.0;
        double longitud = 0.0;
        double min = 0.0;

        if (tipoActual.getTipoDato().equals("Texto")
                || tipoActual.getTipoDato().equals("Número")) {
            listaDatosUtiles.add(dato);
        }
        if(!listaDatosUtiles.isEmpty()){
            View itv = holder.itemView;
            switch (tipoActual.getTipoDato()) {
                case "Texto":
                    String texto = dato.getValorText();
                    longitud = (double) texto.length();
                    max = (double) getMaximoValor(tipoActual);

                    holder.tvCalculo.setText(String.format("(%.0f/%.0f)", longitud, max));

                    setAlturaTexto(longitud, max, tvPorcentaje, itv);
                    break;
                case "Número":
                    max = (double) getMaximoValor(tipoActual);
                    min = (double) getMinimoValor(tipoActual);

                    setAlturaNumero(dato.getValorText(), max, min, holder.tvCalculo,
                            tvPorcentaje, itv);
                    break;
                case "Tipo":
                case "Fecha":
                    holder.tvCalculo.setVisibility(INVISIBLE);
                    holder.tvDato.setVisibility(INVISIBLE);
                    holder.tvPorcentaje.setText(dato.getValorText());
                    holder.tvPorcentaje.setMaxLines(3);
                    break;
            }
        }


        holder.tvPorcentaje.post(new Runnable() {
            @Override
            public void run() {

            }
        });

        holder.tvPorcentaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    private double getMinimoValor(TipoDato tipoActual) {
        double res=0, anterior = 0;
        for (Dato d : listaDatos) {
            double valor=0;
            if(tipoActual.getTipoDato().equals("Número")){
                try{
                    valor = Double.parseDouble(d.getValorText());
                } catch (NumberFormatException ignore){}
                if(valor <= anterior){
                    res = valor;
                }
                anterior = valor;
            }
        }
        return res;
    }

    private double getMaximoValor(TipoDato tipoActual) {
        double res = 0, anterior = 0;
        for (Dato d : listaDatos) {
            if(tipoActual.getTipoDato().equals("Número")){
                double valor=0;
                try{
                    valor = Double.parseDouble(d.getValorText());
                } catch (NumberFormatException ignore){}
                if(valor >= anterior){
                    res = valor;
                }
                anterior = valor;
            }else if(tipoActual.getTipoDato().equals("Texto")){
                double valor = 0;
                try{
                    valor = d.getValorText().length();
                } catch (NumberFormatException ignore){}
                if(valor >= res){
                    res = valor;
                }

            }
        }
        return res;
    }

    private double getMaximaLongitud(TipoDato tipoActual) {
        return 0;
    }
    private double getMinimaLongitud(TipoDato tipoActual) {
        return 0;
    }

    private String getFechaDato(Dato dato) {
        String datosResultado = "";
        Ocurrencia ocu;
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(context,
                                "DBEstudios", null,  DB_VERSION);){
                SQLiteDatabase db;
                db = usdbh.getWritableDatabase();

                ocu = usdbh.getOcurrenciaPorIdYEstudio(db, dato);
                LocalDate date = ocu.getFecha();
                datosResultado = date.getDayOfMonth()+"/"+date.getMonthValue()+"/"+date.getYear();

                db.close();
            } catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        } catch (SQLiteDatabaseCorruptException ex){
            System.out.println(ex.getMessage());
        }
        return datosResultado;
    }

    private void setAlturaNumero(String valorText, double max, double min,
                                 TextView tvCalculo, TextView columna, View itv) {
        double porcentaje;
        double valorDato = 0;
        try{
            valorDato = Double.parseDouble(valorText);
        }catch (NumberFormatException ex){
            System.out.println(ex.getMessage());
        }

        double amplitud = max-min;
        porcentaje = (valorDato-min)/amplitud;

        tvCalculo.setText(String.format("(%.0f/%.0f)", valorDato-min, max-min));
        columna.setText(String.format("%.2f%%", porcentaje * 100));
        //layoutParams(columna, porcentaje, itv);
    }

    private void setAlturaTexto(double longitud, double max, TextView columna, View itv) {
        double porcentaje;
        if(longitud !=0 && max !=0)
            porcentaje = longitud / max;
        else {
            porcentaje = 1;
        }


        columna.setText(String.format("%.2f%%", porcentaje * 100));
        //layoutParams(columna, porcentaje, itv);
        //cambiarAlturaSegura(columna, (int) porcentaje);
    }
    private void layoutParams(View columna, double porcentaje, View itv) {
        int height = columna.getHeight();
        int res = (int) (Math.round(height* porcentaje));

        columna.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }


    public void setListaDatos(ArrayList<Dato> listaDatos) {
        this.listaDatos = listaDatos;
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
    private ArrayList<Dato> getDatos(TipoDato tipo) {
        ArrayList<Dato> datosResultado =new ArrayList<>();
        try{
            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(context,
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
    public int getItemCount() {
        return listaDatos.size();
    }


    public interface OnButtonClickListener {
        void onButtonClickTipo();
    }
    @Override
    public void onButtonClickTipoGrafico(TipoDato tipoDato) {
        this.listaDatos = getDatos(tipoDato);
        tipoActual = tipoDato;
        notifyAll();
    }

}