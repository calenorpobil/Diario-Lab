package com.merlita.diariolab.Adaptadores;

import static android.view.View.GONE;

import static com.merlita.diariolab.MainActivity.DB_VERSION;

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

import com.merlita.diariolab.Modelos.Cualitativo;
import com.merlita.diariolab.Modelos.Dato;
import com.merlita.diariolab.Modelos.Ocurrencia;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.R;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;

import java.util.ArrayList;

public class AdaptadorDatosVer extends RecyclerView.Adapter<AdaptadorDatosVer.MiContenedor> {

    private Context context;
    private ArrayList<TipoDato> listaTipos;
    private ArrayList<Dato> listaDatos;
    private Ocurrencia ocurrencia;

    boolean enabled;

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
        View v = inflador.inflate(R.layout.fila_dato_ver, parent, false);


        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        TipoDato actual = listaTipos.get(holder.getAbsoluteAdapterPosition());
        if (position>=0 && position < listaTipos.size()){
            listaDatos = getDatos();

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


            holder.etNumero.setEnabled(enabled);
            holder.etTexto.setEnabled(enabled);
            holder.tvHora.setEnabled(enabled);

            switch (tipo.getTipoDato()){
                case "Número": {
                    holder.tvHora.setVisibility(GONE);
                    holder.etTexto.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);

                    holder.etNumero.setText(dato.getValorText());
                    break;
                }
                case "Texto": {
                    holder.tvHora.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);

                    holder.etTexto.setText(dato.getValorText());
                    break;
                }
                case "Fecha": {
                    holder.etTexto.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);



                    String nombreTipo = "Escribe una fecha";
                    nombreTipo = tipo.getDescripcion();
                    holder.tvHora.setText(dato.getValorText());

                    break;
                }
                case "Hora": {
                    holder.tvHora.setText(dato.getValorText());
                    holder.etTexto.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    break;
                }
                case "Tipo": {
                    holder.etTexto.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.tvHora.setVisibility(GONE);

                    ArrayList<Cualitativo> listaCualitativos =
                            getCualitativos(tipo.getFkEstudio(), tipo.getNombre());
                    ArrayList<String> aux = new ArrayList<>();
                    for (Cualitativo a: listaCualitativos) {
                        aux.add(a.getTitulo());
                    }
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                            this.context, android.R.layout.simple_spinner_item,
                            aux);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.spTipo.setAdapter(adapter1);
                    int index = aux.indexOf(dato.getValorText());
                    holder.spTipo.setSelection(index);
                    holder.spTipo.setEnabled(enabled);

                    break;
                }
                default:{
                    holder.etTexto.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.tvNombreTipo.setText("Elige una ocurrencia para ver:");
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

    private ArrayList<Dato> getDatos() {
        ArrayList<Dato> res = new ArrayList<>();

        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this.context,
                            "DBEstudios", null, DB_VERSION);){
            db = usdbh.getWritableDatabase();

            res = usdbh.getDatosPorOcurrencia(db, ocurrencia.getCod(), ocurrencia.getFkEstudioN());
        }

        return res;
    }


    private ArrayList<Cualitativo> getCualitativos(String fk_estudio, String fk_tipo) {
        ArrayList<Cualitativo> res = new ArrayList<>();

        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(this.context,
                            "DBEstudios", null, DB_VERSION);){
            db = usdbh.getWritableDatabase();

            res = usdbh.getCualitativos(db, fk_estudio, fk_tipo);
        }

        return res;
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
                             AdaptadorDatosVer.OnButtonClickListener listener, Ocurrencia ocurrencia,
                             ArrayList<TipoDato> listaTipos,
                             DatePickerListener listenerFecha, boolean enabled) {
        super();
        this.context = context;
        this.listaDatos = lista;
        this.listener = listener;
        this.listenerFecha = listenerFecha;
        this.ocurrencia = ocurrencia;
        this.listaTipos = listaTipos;
        this.enabled = enabled;

    }

    @Override
    public int getItemCount() {
        return listaDatos.size();
    }

    public ArrayList<Dato> getLista(){
        return listaDatos;
    }


}
