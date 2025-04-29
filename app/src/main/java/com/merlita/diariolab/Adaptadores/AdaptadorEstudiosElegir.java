package com.merlita.diariolab.Adaptadores;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.MainActivity;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.R;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class AdaptadorEstudiosElegir extends RecyclerView.Adapter<AdaptadorEstudiosElegir.MiContenedor> {

    private Context context;
    private ArrayList<Estudio> lista;

    SQLiteDatabase db;
    private final int DB_VERSION= MainActivity.DB_VERSION;


    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        ConstraintLayout main;
        boolean visible = false;
        TextView tvTitulo, tvDescripcion, tvEmoji, tvCheck;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            main = (ConstraintLayout) itemView.findViewById(R.id.main);

            tvTitulo = (TextView) itemView.findViewById(R.id.tvEstudio1);
            tvDescripcion = (TextView) itemView.findViewById(R.id.tvDescripcion);
            tvEmoji = (TextView) itemView.findViewById(R.id.tvEmoji);
            tvCheck = (TextView) itemView.findViewById(R.id.tvCheck);
            itemView.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenu.ContextMenuInfo contextMenuInfo)
        {
            contextMenu.add(getAdapterPosition(), 121, 0, "EDITAR");
            contextMenu.add(getAdapterPosition(), 122, 1, "BORRAR");
        }
    }





    @NonNull
    @Override
    public MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflador =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflador.inflate(R.layout.fila_estudios_elegir, parent, false);

        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        int reverseIndex = lista.size() -1 -position;
        Estudio estudio = lista.get(holder.getAbsoluteAdapterPosition());
        holder.tvTitulo.setText(estudio.getNombre());
        holder.tvDescripcion.setText(estudio.getDescripcion());
        holder.tvEmoji.setText(estudio.getEmoji());

        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((holder.tvCheck.getVisibility() == VISIBLE)){
                    holder.tvCheck.setVisibility(INVISIBLE);
                }else{
                    holder.tvCheck.setVisibility(VISIBLE);
                }
            }
        });
    }

    private int getOcurrencia(String estudios) {
        int res = -1;


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(context,
                            "DBEstudios", null,  DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getOcurrencia(db, estudios);

            db.close();
        }

        return res;
    }


    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult");
    }


    public AdaptadorEstudiosElegir(Context context, ArrayList<Estudio> lista) {
        super();
        this.context = context;
        this.lista = lista;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class FragmentoFecha extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private DatePickerDialog.OnDateSetListener listener;

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }
        public static FragmentoFecha newInstance(DatePickerDialog.OnDateSetListener listener) {
            FragmentoFecha fragment = new FragmentoFecha();
            fragment.setListener(listener);
            return fragment;
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
        {
            final Calendar c = Calendar.getInstance();
            return new DatePickerDialog(
                    requireContext(),
                    (view, year, month, day) -> {
                        if (listener != null) {
                            listener.onDateSet(view, year, month, day);
                        }
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
        }


        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            if (listener != null) {
                listener.onDateSet(datePicker, year, month, day);
            }

        }

    }
}
