package com.merlita.diariolab.Adaptadores;

import android.app.Activity;
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
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.MainActivity;
import com.merlita.diariolab.Modelos.Analisis;
import com.merlita.diariolab.Modelos.CircleItem;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.R;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdaptadorAnalisis extends RecyclerView.Adapter<AdaptadorAnalisis.MiContenedor> {

    private static final int GRID_SIZE = 5;
    private final Activity activity;
    private Context context;
    private Estudio estudio1, estudio2;
    SQLiteDatabase db;
    private final int DB_VERSION= MainActivity.DB_VERSION;
    private ArrayList<Analisis> lista;

    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        ConstraintLayout main;
        TextView tvEstudio2, tvEstudio1, tvEmoji1, tvEmoji2;
        GridView gvGrafico;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            main = (ConstraintLayout) itemView.findViewById(R.id.main);

            tvEstudio2 = (TextView) itemView.findViewById(R.id.tvEstudio2);
            tvEstudio1 = (TextView) itemView.findViewById(R.id.tvEstudio1);
            tvEmoji1 = (TextView) itemView.findViewById(R.id.tvEmoji1);
            tvEmoji2 = (TextView) itemView.findViewById(R.id.tvEmoji2);
            gvGrafico = (GridView) itemView.findViewById(R.id.gvGrafico);

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
        View v = inflador.inflate(R.layout.fila_analisis, parent, false);

        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        Analisis actual = lista.get(holder.getAbsoluteAdapterPosition());
        Estudio estudio1 = actual.getEstudio1();
        Estudio estudio2 = actual.getEstudio2();
        holder.tvEstudio2.setText(estudio2.getNombre());
        holder.tvEmoji2.setText(estudio2.getEmoji());
        holder.tvEstudio1.setText(estudio1.getNombre());
        holder.tvEmoji1.setText(estudio1.getEmoji());

        holder.gvGrafico.setNumColumns(GRID_SIZE);
        ArrayList<CircleItem> items = new ArrayList<>();
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int diameter = 20 + (int)(Math.random() * 81); // 20-100
                items.add(new CircleItem(diameter, x, y));
            }
        }
        AdaptadorGridAnalisis adapter;
        adapter = new AdaptadorGridAnalisis(activity, items);

        holder.gvGrafico.setAdapter(adapter);




        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

            res = usdbh.getCuentaOcurrencias(db, estudios);

            db.close();
        }

        return res;
    }


    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult");
    }


    public AdaptadorAnalisis(Context context, Activity activity,
                             ArrayList<Analisis> lista) {
        super();
        this.activity = activity;
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
