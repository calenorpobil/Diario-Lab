package com.merlita.diariolab.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.R;

import java.util.ArrayList;

public class AdaptadorTabla extends RecyclerView.Adapter<AdaptadorTabla.ViewHolder> {
    private final ArrayList<ArrayList<String>> datosTabla;
    private final LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;

    private int numColumnas;


    AdaptadorTabla(Context context, ArrayList<ArrayList<String>> data) {
        this.layoutInflater = LayoutInflater.from(context);
        this.datosTabla = data;
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