package com.merlita.diariolab.Adaptadores;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.R;

import java.util.ArrayList;

public class AdaptadorTabla extends RecyclerView.Adapter<AdaptadorTabla.ViewHolder> {
    private final ArrayList<ArrayList<String>> datosTabla;
    private final LayoutInflater layoutInflater;
    private final Activity activity;
    private ItemClickListener itemClickListener;

    private int numColumnas;
    private Point p;
    private int anchoPantalla, altoPantalla;


    AdaptadorTabla(Context context, ArrayList<ArrayList<String>> data, Activity activity) {
        this.layoutInflater = LayoutInflater.from(context);
        this.datosTabla = data;
        this.activity = activity;
    }

    // Infla el layout de la celda desde el xml
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_analisis_cell, parent, false);
        return new ViewHolder(view);
    }

    // Número total de celdas
    @Override
    public int getItemCount() {
        return datosTabla.size() * datosTabla.get(0).size();
    }


    // Almacena y recicla vistas mientras entran y salen de la pantalla
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        View vwCircle;

        public ViewHolder(View itemView) {
            super(itemView);

            myTextView = itemView.findViewById(R.id.info_text);
            vwCircle = itemView.findViewById(R.id.vwCirculo);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAbsoluteAdapterPosition());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int totalColumnas = datosTabla.get(0).size();
        int fila = position / totalColumnas;
        int columna = position % totalColumnas;

        String valor = datosTabla.get(fila).get(columna);
        holder.myTextView.setText(valor);

        if (fila!=0 && columna!=0){
            p = new Point();
            Display pantallaDisplay = activity.getWindowManager().getDefaultDisplay();
            pantallaDisplay.getSize(p);
            anchoPantalla = p.x;
            altoPantalla = p.y;

            int anchoCircle = (int) (anchoPantalla / (numColumnas*1.5/1000));
//            anchoCircle = 200;
            anchoCircle = (int) (anchoCircle/(Math.pow(10, 8)))*4;
            int datoAncho=0;
            try {
                datoAncho = Integer.parseInt(datosTabla.get(fila).get(columna));
            }catch (NumberFormatException ignored){}
            anchoCircle = anchoCircle*datoAncho/100;
            ViewGroup.LayoutParams lp =
                    new LinearLayout.LayoutParams(anchoCircle, anchoCircle);
            holder.myTextView.setVisibility(GONE);
            lp.width = anchoCircle;
            lp.height = anchoCircle;
            holder.vwCircle.setBackgroundResource(R.drawable.circle_shape);
            holder.vwCircle.setVisibility(VISIBLE);
            holder.vwCircle.setLayoutParams(lp);
        }
    }


    String getItem(int id) {
        return "-1";
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // La actividad padre implementará este método para responder a eventos de click
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}