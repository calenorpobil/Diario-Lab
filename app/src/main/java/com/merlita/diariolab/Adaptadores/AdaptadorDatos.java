package com.merlita.diariolab.Adaptadores;

import static android.view.View.GONE;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;
import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.R;

import java.util.ArrayList;

public class AdaptadorDatos extends RecyclerView.Adapter<AdaptadorDatos.MiContenedor> {

    private Context context;
    private ArrayList<TipoDato> listaTipos;
    private ArrayList<Dato> listaDatos;

    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    private AdaptadorDatos.OnButtonClickListener listener;

    protected DatePickerListener listenerFecha;

    SQLiteDatabase db;


    public static class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        EditText etDescripcion, etNumero;
        TextView tvNombreTipo;
        Spinner spTipo;
        TextView tvHora;


        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            etDescripcion = (EditText) itemView.findViewById(R.id.etDescripcion);
            tvHora = (TextView) itemView.findViewById(R.id.tvDatoHora);
            spTipo = (Spinner) itemView.findViewById(R.id.spTipoDato);
            etNumero = (EditText) itemView.findViewById(R.id.etNumero);
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
        TipoDato tipo = listaTipos.get(holder.getAbsoluteAdapterPosition());



        holder.tvNombreTipo.setText(listaTipos.get(position).getNombre());

        switch (tipo.getTipoDato()){
            case "Número": {
                holder.tvHora.setVisibility(GONE);
                holder.etDescripcion.setVisibility(GONE);
                holder.spTipo.setVisibility(GONE);

                holder.etNumero.setHint(tipo.getDescripcion());

                break;
            }
            case "Texto": {
                holder.tvHora.setVisibility(GONE);
                holder.etNumero.setVisibility(GONE);
                holder.spTipo.setVisibility(GONE);

                holder.etDescripcion.setHint(tipo.getDescripcion());
                break;
            }
            case "Fecha": {
                holder.etDescripcion.setVisibility(GONE);
                holder.etNumero.setVisibility(GONE);
                holder.spTipo.setVisibility(GONE);



                String nombreTipo = "Escribe una fecha";
                nombreTipo = tipo.getDescripcion();
                holder.tvHora.setHint(nombreTipo);

                break;
            }
            case "Hora": {
                holder.tvHora.setText(dato.getValorText());
                holder.etDescripcion.setVisibility(GONE);
                holder.spTipo.setVisibility(GONE);
                break;
            }


        }


        holder.tvHora.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                // Accede al fragment manager desde el contexto
                FragmentManager fragmentManager = ((AppCompatActivity) v.getContext())
                        .getSupportFragmentManager();

                // Crea y muestra el DatePicker
                AdaptadorEstudios.FragmentoFecha datePicker = new AdaptadorEstudios.FragmentoFecha();
                datePicker.setListener((view, year, month, day) -> {
                    String fecha = day+"/"+(month+1)+"/"+year;
                    holder.tvHora.setText(fecha); // Actualiza directamente la vista

                    // Opcional: Actualiza también el modelo si lo necesitas
                    // ((AdaptadorTiposDato) getAdapter()).actualizarFecha(position, fecha);
                });
                datePicker.show(fragmentManager, "datePicker");
            }
        });




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
        holder.etDescripcion.addTextChangedListener(new TextWatcher() {
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
        holder.etNumero.addTextChangedListener(new TextWatcher() {
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

        holder.tvHora.addTextChangedListener(new TextWatcher() {
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

    public void actualizarFecha(int position, int year, int month, int day) {
        Dato item = listaDatos.get(position);
        String fecha =  String.format("%02d/%02d/%d", day, month + 1, year);
        item.setValorText(fecha);

        //item.setValorText("Alberto");
        //notifyItemChanged(position);
    }

    public interface DatePickerListener {
        void mostrarDatePicker(int position);
    }


    public AdaptadorDatos(Context context, ArrayList<Dato> lista,
                          AdaptadorDatos.OnButtonClickListener listener,
                          ArrayList<TipoDato> listaTipos,
                          DatePickerListener listenerFecha) {
        super();
        this.context = context;
        this.listaDatos = lista;
        this.listener = listener;
        this.listenerFecha = listenerFecha;
        this.listaTipos = listaTipos;
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
