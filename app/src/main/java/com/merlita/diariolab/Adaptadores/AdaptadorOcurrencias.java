package com.merlita.diariolab.Adaptadores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class AdaptadorOcurrencias extends RecyclerView.Adapter<AdaptadorOcurrencias.MiContenedor> {

    private Context context;
    private ArrayList<Ocurrencia> listaOcurrencias;
    private Estudio estudio;
    private int selectedPosition=-1;

    public interface OnButtonClickListener {
        void onButtonClickOcurrencia(Ocurrencia ocurrencia);
    }

    private AdaptadorOcurrencias.OnButtonClickListener listener;



    SQLiteDatabase db;


    public static class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {

        TextView tvRepeticiones, tvFecha;
        Button btEditar;
        ConstraintLayout clFila;


        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            tvFecha = (TextView) itemView.findViewById(R.id.tvFecha);
            tvRepeticiones = (TextView) itemView.findViewById(R.id.tvRepeticiones);
            btEditar = (Button) itemView.findViewById(R.id.btEditar);
            clFila = (ConstraintLayout) itemView.findViewById(R.id.clFila);

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
    public MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflador =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflador.inflate(R.layout.fila_ocurrencia, parent, false);


        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        Ocurrencia ocurrencia = listaOcurrencias.get(holder.getAbsoluteAdapterPosition());
        holder.clFila.setBackgroundResource(0);

        String reps = (position+1)+"";
        holder.tvRepeticiones.setText("🔁 "+reps);
        LocalDate date = ocurrencia.getFecha();
        String fecha = date.getDayOfMonth()+"/"+date.getMonthValue()+"/"+date.getYear();
        holder.tvFecha.setText(fecha);


        // Cambiar el fondo según la selección
        if (position == selectedPosition) {
            holder.clFila.setBackgroundResource(R.color.grisSeleccionado);
        } else {
            holder.clFila.setBackgroundResource(R.color.fondoOcurrencia);
        }




        holder.btEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.clFila.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                listener.onButtonClickOcurrencia(ocurrencia);
                setSelectedPosition(holder.getAbsoluteAdapterPosition());
            }

        });
    }

    // Mét odo para actualizar la selección
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // Actualiza todas las celdas
    }


    public void actualizarFecha(int position, int year, int month, int day) {
        Ocurrencia item = listaOcurrencias.get(position);
        String fecha =  String.format("%02d/%02d/%d", day, month + 1, year);
        item.setFecha(LocalDate.parse(fecha));

        //item.setValorText("Alberto");
        //notifyItemChanged(position);
    }

    public interface DatePickerListener {
        void mostrarDatePicker(int position);
    }



    @Override
    public int getItemCount() {
        int size = 0;
        if(listaOcurrencias!=null){
            size = listaOcurrencias.size();
        }
        return size;
    }

    public AdaptadorOcurrencias(Context context, Estudio estudio, ArrayList<Ocurrencia> lista,
                                OnButtonClickListener listener) {
        super();
        this.context = context;
        this.estudio = estudio;
        this.listaOcurrencias = lista;
        this.listener = listener;
    }






    public AdaptadorOcurrencias(@NonNull Context context) {
        super();
    }


}
