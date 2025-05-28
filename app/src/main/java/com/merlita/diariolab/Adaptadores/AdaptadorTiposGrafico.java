package com.merlita.diariolab.Adaptadores;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.R;

import java.util.ArrayList;

public class AdaptadorTiposGrafico extends RecyclerView.Adapter<AdaptadorTiposGrafico.MiContenedor> {


    private Context context;
    private ArrayList<TipoDato> listaTiposDato;
    private AdaptadorTiposGrafico.OnButtonClickListener listener;
    private int selectedPosition=-1;


    public AdaptadorTiposGrafico(Context context,
                                 ArrayList<TipoDato> listaTiposDato,
                                 OnButtonClickListener listener){
        this.context = context;
        this.listaTiposDato = listaTiposDato;
        this.listener = listener;
    }

    public static class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {

        Button btTipo;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            btTipo = (Button) itemView.findViewById(R.id.btMedidas);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenu.ContextMenuInfo contextMenuInfo)
        {
            contextMenu.add(getAbsoluteAdapterPosition(), 121, 1, "BORRAR");
        }

    }

    @NonNull
    @Override
    public AdaptadorTiposGrafico.MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflador =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflador.inflate(R.layout.fila_tipos_grafico, parent, false);


        return new AdaptadorTiposGrafico.MiContenedor(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorTiposGrafico.MiContenedor holder, int position) {
        int posicion = holder.getAbsoluteAdapterPosition();
        TipoDato tipoDato = listaTiposDato.get(posicion);

        holder.btTipo.setText(tipoDato.getNombre());

        // Cambiar el fondo según la selección
        if (position == selectedPosition) {
            holder.btTipo.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.fondoRectanguloClick));
        } else {
            holder.btTipo.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.fondoRectangulo));
        }

        holder.btTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedPosition(posicion);
                listener.onButtonClickTipoGrafico(tipoDato);
            }
        });

    }

    // Mét odo para actualizar la selección
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // Actualiza todas las celdas
    }


    @Override
    public int getItemCount() {
        int size = 0;
        if(listaTiposDato!=null){
            size = listaTiposDato.size();
        }
        return size;
    }


    public interface OnButtonClickListener {
        void onButtonClickTipoGrafico(TipoDato tipoDato);
    }


}