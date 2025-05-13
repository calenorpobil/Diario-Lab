package com.merlita.diariolab.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.R;

import java.util.List;

public class AdaptadorTabla extends RecyclerView.Adapter<AdaptadorTabla.ViewHolder> {
    private final List<List<String>> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    AdaptadorTabla(Context context, List<List<String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_analisis_cell, parent, false);
        return new ViewHolder(view);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size() * mData.get(0).size();
    }


    // stores and recycles views as they are scrolled off screen
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
            if (mClickListener != null) mClickListener.onItemClick(view, getAbsoluteAdapterPosition());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int totalColumnas = mData.get(0).size();  // Ejemplo:
        int fila = position / totalColumnas;    // División entera
        int columna = position % totalColumnas; // Resto

        String valor = mData.get(fila).get(columna);
        holder.myTextView.setText(valor);
    }


    // convenience method for getting data at click position
    String getItem(int id) {
        return "-1";
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}