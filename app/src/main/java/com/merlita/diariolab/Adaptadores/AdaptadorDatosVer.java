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
    private boolean primeraVez = true;
    private int cuenta=0;

    public interface OnButtonClickListener {
        void onButtonClickDatos(ArrayList<Dato> datos);
    }

    private OnButtonClickListener listener;

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
        TipoDato tdActual = listaTipos.get(holder.getAbsoluteAdapterPosition());
        Dato datoActual = new Dato();
        if (position>=0 && position < listaTipos.size()){
            if (primeraVez){
                listaDatos = getDatos();
                primeraVez=false;
            }
            cuenta++;
            for (int i = 0; i < listaDatos.size(); i++) {
                String fkDato = listaDatos.get(i).getFkTipoDato();
                String nomTipo = tdActual.getId()+"";
                if(fkDato.equals(nomTipo)) {
                    datoActual = listaDatos.get(i);
                    break;
                }
            }
            if (listaDatos.isEmpty()){

            }


            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this.context,
                    //Cambiar tipos:
                    R.array.tipos,
                    android.R.layout.simple_spinner_item
            );


            holder.tvNombreTipo.setText(listaTipos.get(position).getNombre());


            holder.etNumero.setEnabled(enabled);
            holder.etTexto.setEnabled(enabled);
            holder.tvHora.setEnabled(enabled);

            String texto = datoActual.getValorText();
            if ((texto.isEmpty() || texto.equals(" ")) && !enabled) {
                texto = "Sin datos";
            }
            switch (tdActual.getTipoDato()){
                case "Número": {
                    holder.tvHora.setVisibility(GONE);
                    holder.etTexto.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);

                    holder.etNumero.setText(texto);
                    break;
                }
                case "Texto": {
                    holder.tvHora.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);

                    holder.etTexto.setText(texto);
                    break;
                }
                case "Fecha": {
                    holder.etTexto.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);

                    if(enabled && datoActual.getValorText().isEmpty()){
                        texto = "Elige una fecha";
                    }
                    holder.tvHora.setText(texto);
                    break;
                }
                case "Hora": {      //Dato no codificado todavía
                    holder.tvHora.setText(datoActual.getValorText());
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
                            getCualitativos(tdActual.getFkEstudio(), tdActual.getId()+"");
                    ArrayList<String> cualitativos = new ArrayList<>();
                    for (Cualitativo c: listaCualitativos) {
                        cualitativos.add(c.getTitulo());
                    }

                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                            this.context, android.R.layout.simple_spinner_item,
                            cualitativos);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.spTipo.setAdapter(adapter1);
                    int index = cualitativos.indexOf(datoActual.getValorText());
                    if(index==-1){
                        cualitativos.add("Sin datos");
                        holder.spTipo.setSelection(cualitativos.size()-1);
                    } else {
                        holder.spTipo.setSelection(index);
                    }
                    holder.spTipo.setEnabled(enabled);
                    cualitativos.add("Sin datos");

                    break;
                }
                default:{
                    holder.etTexto.setVisibility(GONE);
                    holder.spTipo.setVisibility(GONE);
                    holder.etNumero.setVisibility(GONE);
                }
            }

            holder.tvHora.setOnClickListener(v -> {
                if (holder.getAbsoluteAdapterPosition() != RecyclerView.NO_POSITION) {
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
                String texto = s.toString();
                int index = getDatoPorIndexTipos(holder.getAbsoluteAdapterPosition());
                if(index!=-1){
                    listaDatos.get(index).setValorText(texto);
                    listener.onButtonClickDatos(listaDatos);
                }
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
                String texto = s.toString();
                int index = getDatoPorIndexTipos(holder.getAbsoluteAdapterPosition());
                if(index!=-1){
                    listaDatos.get(index).setValorText(texto);
                    listener.onButtonClickDatos(listaDatos);
                }
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
                listener.onButtonClickDatos(listaDatos);
            }
            // ... (métodos onTextChanged y beforeTextChanged vacíos)
        });

        holder.spTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                int posicion = holder.getAbsoluteAdapterPosition();
                String texto = parent.getItemAtPosition(pos).toString();
                Dato dato;
                if(posicion<listaDatos.size()){
                    dato = listaDatos.get(posicion);
                    dato.setValorText(texto);
                }else{
                    listaDatos.add(new Dato(listaTipos.get(posicion),
                            ocurrencia.getFkEstudioN(), ocurrencia.getCod(), texto));
                }
                listener.onButtonClickDatos(listaDatos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó nada
            }
        });
    }

    private int getDatoPorIndexTipos(int posicion) {
        int index = -1;
        TipoDato elegido = listaTipos.get(posicion);
        for (Dato d :
                listaDatos) {
            if ((elegido.getId()+"").equals(d.getFkTipoDato())) {

                for (int i = 0; i < listaDatos.size(); i++) {
                    if(listaDatos.get(i).getFkTipoDato().equals(d.getFkTipoDato())) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }


    private Dato datoDeTipo(TipoDato tipoDato) {
        Dato correspondiente = new Dato();

        for (Dato d : listaDatos) {
            if (d.getFkTipoDato().equals(tipoDato.getNombre())) {
                correspondiente = d;
                break;
            }
        }

        return correspondiente;
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


    public AdaptadorDatosVer(Context context, ArrayList<TipoDato> listaTipos,
                             OnButtonClickListener listener, Ocurrencia ocurrencia,
                             ArrayList<Dato> lista,
                             DatePickerListener listenerFecha, boolean enabled) {
        super();
        this.context = context;
        this.listaTipos = listaTipos;
        this.listaDatos = lista;
        this.listener = listener;
        this.listenerFecha = listenerFecha;
        this.ocurrencia = ocurrencia;
        this.enabled = enabled;

    }

    @Override
    public int getItemCount() {
        return listaTipos.size();
    }

    public ArrayList<Dato> getLista(){
        return listaDatos;
    }


}
