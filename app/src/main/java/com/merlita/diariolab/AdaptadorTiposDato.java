package com.merlita.diariolab;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Modelos.Estudio;

import java.util.ArrayList;

public class AdaptadorTiposDato extends RecyclerView.Adapter<AdaptadorTiposDato.MiContenedor> {

    private Context context;
    private ArrayList<TipoDato> lista;
    String[] ordenSpinner = {"Número", "Texto", "Fecha"};

    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    private OnButtonClickListener listener;
    Estudio estudioFila;




    SQLiteDatabase db;



    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        EditText etNombre, etDescripcion;
        Spinner spTipoDato;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            etNombre = (EditText) itemView.findViewById(R.id.etNombre);
            etDescripcion = (EditText) itemView.findViewById(R.id.etDescripcion);
            spTipoDato = (Spinner) itemView.findViewById(R.id.spTipoDato);
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
        View v = inflador.inflate(R.layout.fila_tipo_dato, parent, false);


        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        TipoDato tipoDato = lista.get(holder.getAbsoluteAdapterPosition());
        holder.etDescripcion.setText(tipoDato.getDescripcion());
        holder.etNombre.setText(tipoDato.getNombre());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.context,
                R.array.tipos,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spTipoDato.setAdapter(adapter);


        holder.spTipoDato.setSelection(adapter.getPosition(tipoDato.getTipoDato()));


        // Establecer listeners
        holder.etNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                lista.get(holder.getAbsoluteAdapterPosition()).setNombre(s.toString());
            }
            // ... (métodos onTextChanged y beforeTextChanged vacíos)
        });

        holder.etDescripcion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                lista.get(holder.getAbsoluteAdapterPosition()).setDescripcion(s.toString());
            }
            // ... (métodos onTextChanged y beforeTextChanged vacíos)
        });

        holder.spTipoDato.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                lista.get(holder.getAbsoluteAdapterPosition()).setTipoDato(
                        parent.getItemAtPosition(pos).toString()
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó nada
            }
        });



    }


    public AdaptadorTiposDato(Context context, ArrayList<TipoDato> lista,
                              OnButtonClickListener listener) {
        super();
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public ArrayList<TipoDato> getLista(){
        return lista;
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



    public AdaptadorTiposDato(@NonNull Context context) {
        super();
    }


}
