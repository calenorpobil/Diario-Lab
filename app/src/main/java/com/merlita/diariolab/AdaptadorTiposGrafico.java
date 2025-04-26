package com.merlita.diariolab;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Adaptadores.AdaptadorOcurrencias;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Modelos.TipoDato;

import java.util.ArrayList;

public class AdaptadorTiposGrafico extends RecyclerView.Adapter<AdaptadorTiposGrafico.MiContenedor> {


    private Context context;
    private ArrayList<TipoDato> listaTiposDato;
    private AdaptadorTiposGrafico.OnButtonClickListener listener;


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

            btTipo = (Button) itemView.findViewById(R.id.btTipo);

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
        TipoDato tipoDato = listaTiposDato.get(holder.getAbsoluteAdapterPosition());

        holder.btTipo.setText(tipoDato.getNombre());


        holder.btTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClickTipo();
            }
        });

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
        void onButtonClickTipo();
    }


}
