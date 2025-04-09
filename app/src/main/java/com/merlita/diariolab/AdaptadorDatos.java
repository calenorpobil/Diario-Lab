package com.merlita.diariolab;

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
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.TipoDato;

import java.util.ArrayList;

public class AdaptadorDatos extends RecyclerView.Adapter<AdaptadorDatos.MiContenedor> {

    private Context context;
    private ArrayList<TipoDato> listaTipos;
    private ArrayList<Dato> listaDatos;

    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    private AdaptadorDatos.OnButtonClickListener listener;


    SQLiteDatabase db;



    public static class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        EditText etTexto, etHora;
        TextView tvNombreTipo;
        Spinner spTipo;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            etTexto = (EditText) itemView.findViewById(R.id.etDescripcion);
            etHora = (EditText) itemView.findViewById(R.id.etDatoHora);
            spTipo = (Spinner) itemView.findViewById(R.id.spTipoDato);
            tvNombreTipo = (TextView) itemView.findViewById(R.id.tvNombre);
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
        View v = inflador.inflate(R.layout.fila_dato, parent, false);


        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        Dato dato = listaDatos.get(holder.getAbsoluteAdapterPosition());
        holder.etHora.setText(dato.getValorText());
        holder.etTexto.setText(dato.getFkTipoDato());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.context,
                //Cambiar tipos:
                R.array.tipos,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spTipo.setAdapter(adapter);


        holder.spTipo.setSelection(adapter.getPosition(dato.getFkTipoDato()));


        // Establecer listeners
        holder.etTexto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                listaDatos.get(holder.getAbsoluteAdapterPosition()).setFkTipoDato(s.toString());
            }
            // ... (métodos onTextChanged y beforeTextChanged vacíos)
        });

        holder.etHora.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                listaDatos.get(holder.getAbsoluteAdapterPosition()).setValorText(s.toString());
            }
            // ... (métodos onTextChanged y beforeTextChanged vacíos)
        });

        holder.spTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                listaDatos.get(holder.getAbsoluteAdapterPosition()).setValorText(
                        parent.getItemAtPosition(pos).toString()
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó nada
            }
        });



    }


    public AdaptadorDatos(Context context, ArrayList<Dato> lista,
                          AdaptadorDatos.OnButtonClickListener listener) {
        super();
        this.context = context;
        this.listaDatos = lista;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaDatos.size();
    }

    public ArrayList<Dato> getLista(){
        return listaDatos;
    }


    private void configurarSpinner(Spinner spinner, int arrayResId, String value) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.context,
                arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (value != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }


    private long editarSQL(Estudio nuevo, int nuevaCuenta){
        long res=-1;
        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this.context,
                            "DBEstudios", null, 1);){
            db = usdbh.getWritableDatabase();

            res = usdbh.editarSQL(db, nuevo, nuevaCuenta);


        }
        return res;
    }



    public AdaptadorDatos(@NonNull Context context) {
        super();
    }


}
