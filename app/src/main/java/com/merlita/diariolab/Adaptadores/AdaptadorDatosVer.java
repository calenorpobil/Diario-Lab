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

import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.R;

import java.util.ArrayList;

public class AdaptadorDatosVer extends RecyclerView.Adapter<AdaptadorDatosVer.MiContenedor> {

    private Context context;
    private ArrayList<TipoDato> listaTipos;
    private ArrayList<Dato> listaDatos;

    public interface OnButtonClickListener {
        void onButtonClickDatos();
    }

    private AdaptadorDatosVer.OnButtonClickListener listener;

    protected DatePickerListener listenerFecha;

    SQLiteDatabase db;


    public static class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        EditText etTexto, etNumero;
        TextView tvNombreTipo;
        Spinner spTipo;
        TextView tvHora;


        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            etTexto = (EditText) itemView.findViewById(R.id.etDescripcion);
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
        View v = inflador.inflate(R.layout.columna_dato, parent, false);


        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        if (position>=0 && position < listaTipos.size()){

            Dato dato = listaDatos.get(holder.getAbsoluteAdapterPosition());
            TipoDato tipo = listaTipos.get(holder.getAbsoluteAdapterPosition());

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this.context,
                    //Cambiar tipos:
                    R.array.tipos,
                    android.R.layout.simple_spinner_item
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spTipo.setAdapter(adapter);

            holder.spTipo.setSelection(adapter.getPosition(dato.getFkTipoDato()));

            holder.tvNombreTipo.setText(listaTipos.get(position).getNombre());

            switch (tipo.getTipoDato()){
                case "Número": {
                    holder.tvHora.setVisibility(GONE);
                    holder.etTexto.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);

                    holder.etNumero.setText(dato.getValorText());
                    holder.etNumero.setEnabled(false);
                    break;
                }
                case "Texto": {
                    holder.tvHora.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);

                    holder.etTexto.setText(dato.getValorText());
                    holder.etTexto.setEnabled(false);
                    break;
                }
                case "Fecha": {
                    holder.etTexto.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);



                    String nombreTipo = "Escribe una fecha";
                    nombreTipo = tipo.getDescripcion();
                    holder.tvHora.setText(dato.getValorText());
                    holder.tvHora.setEnabled(false);

                    break;
                }
                case "Hora": {
                    holder.tvHora.setText(dato.getValorText());
                    holder.tvHora.setEnabled(false);
                    holder.etTexto.setVisibility(GONE);
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

                    });
                    datePicker.show(fragmentManager, "datePicker");
                }
            });

        }

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
                /*listaDatos.get(holder.getAbsoluteAdapterPosition()).setValorText(
                        parent.getItemAtPosition(pos).toString()
                );*/
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


    public AdaptadorDatosVer(Context context, ArrayList<Dato> lista,
                             AdaptadorDatosVer.OnButtonClickListener listener,
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
    public AdaptadorDatosVer(@NonNull Context context) {
        super();
    }


}
