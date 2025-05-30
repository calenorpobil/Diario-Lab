package com.merlita.diariolab.Adaptadores;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Cualitativo;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.R;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;

public class AdaptadorCualitativo extends RecyclerView.Adapter<AdaptadorCualitativo.MiContenedor> {

    private Context context;
    private ArrayList<Cualitativo> lista;
    int cuenta=0;
    SQLiteDatabase db;

    public interface OnButtonClickListener {
        void onButtonClickQuitarCualitativo(int pos);
    }
    private static AdaptadorCualitativo.OnButtonClickListener listener;

    public static class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        EditText etTitulo;
        TextView tvCualitativo;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            etTitulo = (EditText) itemView.findViewById(R.id.etNombreCualitativo);
            tvCualitativo = (TextView) itemView.findViewById(R.id.tvCualitativo);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenu.ContextMenuInfo contextMenuInfo)
        {
            contextMenu.add(getAbsoluteAdapterPosition(), 122, 1, "Borrar el tipo");
        }

    }



    @NonNull
    @Override
    public MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflador =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflador.inflate(R.layout.fila_cualitativo, parent, false);

        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        Cualitativo cualitativo = lista.get(holder.getAbsoluteAdapterPosition());

        String texto = "Tipo "+(++cuenta)+": ";
        holder.tvCualitativo.setText(texto);
        holder.etTitulo.setText(cualitativo.getTitulo());

        holder.etTitulo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                lista.get(holder.getAbsoluteAdapterPosition()).setTitulo(s.toString());
            }
            // ... (métodos onTextChanged y beforeTextChanged vacíos)
        });

    }


    public AdaptadorCualitativo(Context context, ArrayList<Cualitativo> lista) {
        super();
        this.context = context;
        this.lista = lista;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public ArrayList<Cualitativo> getLista(){
        return lista;
    }




}
