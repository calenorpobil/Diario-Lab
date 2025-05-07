package com.merlita.diariolab.Adaptadores;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Cualitativo;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.R;

import java.util.ArrayList;

public class AdaptadorTiposDato extends RecyclerView.Adapter<AdaptadorTiposDato.MiContenedor> {

    private Context context;
    private ArrayList<TipoDato> lista;
    ArrayList<Cualitativo> listaCualitativo = new ArrayList<>();
    private boolean visible = false;
    private String nombreEstudio;
    private boolean primera = true;
    String[] ordenSpinner = {"Número", "Texto", "Fecha", "Tipo"};

    public interface OnButtonClickListener {
        void onButtonClickNuevoCualitativo(Cualitativo nuevo);
    }
    private AdaptadorTiposDato.OnButtonClickListener listener;

    Estudio estudioFila;



    SQLiteDatabase db;



    public static class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        EditText etNombre, etDescripcion;
        Spinner spTipoDato;
        RecyclerView rvCualitativos;
        ConstraintLayout segundo;
        Button btNuevoCualitativo;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            etNombre = (EditText) itemView.findViewById(R.id.etNombre);
            etDescripcion = (EditText) itemView.findViewById(R.id.etDescripcion);
            spTipoDato = (Spinner) itemView.findViewById(R.id.spTipoDato);
            rvCualitativos = (RecyclerView) itemView.findViewById(R.id.rvNuevosTipos);
            segundo = (ConstraintLayout) itemView.findViewById(R.id.segundo);
            btNuevoCualitativo = (Button) itemView.findViewById(R.id.btNuevoCualitativo);

            itemView.setOnCreateContextMenuListener(this);
        }





        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenu.ContextMenuInfo contextMenuInfo)
        {
            contextMenu.add(getAbsoluteAdapterPosition(), 121, 1, "Borrar Tipo de Dato");
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
//        tipoDato.setFkEstudio(nombreEstudio);

        holder.etDescripcion.setText(tipoDato.getDescripcion());
        holder.etNombre.setText(tipoDato.getNombre());



        AdaptadorCualitativo adaptadorCualitativo =
                new AdaptadorCualitativo(context, listaCualitativo);
        holder.rvCualitativos.setLayoutManager(new LinearLayoutManager(context));
        holder.rvCualitativos.setAdapter(adaptadorCualitativo);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.context,
                R.array.tipos,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spTipoDato.setAdapter(adapter);
        holder.spTipoDato.setSelection(adapter.getPosition(tipoDato.getTipoDato()));


        holder.spTipoDato.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int tamanyo = adapter.getCount()-1;
                tipoDato.setTipoDato(ordenSpinner[position]);

                //TIPO: CUALITATIVO
                if( position == tamanyo){
                    listaCualitativo = getCualitativos(tipoDato.getFkEstudio(), tipoDato.getNombre());
                    holder.segundo.setVisibility(VISIBLE);
                }else{
                    holder.segundo.setVisibility(GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        holder.btNuevoCualitativo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cualitativo nuevo = new Cualitativo();
                nuevo.setFk_dato_tipo_t(holder.etNombre.getText().toString());
                listaCualitativo.add(0, nuevo);
                adaptadorCualitativo.notifyItemInserted(0);
                listener.onButtonClickNuevoCualitativo(nuevo);
            }
        });


        // Establecer listeners
        holder.etNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                lista.get(holder.getAbsoluteAdapterPosition()).setNombre(s.toString());
                for (int i = 0; i < listaCualitativo.size(); i++) {
                    listaCualitativo.get(i).setFk_dato_tipo_t(s.toString());
                }
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
    }

    private ArrayList<Cualitativo> getCualitativos(String fk_estudio, String fk_tipo) {
        ArrayList<Cualitativo> res = new ArrayList<>();
        if(primera){
           primera = false;

            try(EstudiosSQLiteHelper usdbh =
                        new EstudiosSQLiteHelper(this.context,
                                "DBEstudios", null, DB_VERSION);){
                db = usdbh.getWritableDatabase();

                res = usdbh.getCualitativos(db, fk_estudio, fk_tipo);
            }

            for (int i = 0; i < res.size(); i++) {
                listener.onButtonClickNuevoCualitativo(res.get(i));
            }

            this.notifyDataSetChanged();

            return res;
        }
        return listaCualitativo;
    }


    public AdaptadorTiposDato(Context context, ArrayList<TipoDato> lista, String nombreEstudio,
                              OnButtonClickListener listener) {
        super();
        this.context = context;
        this.lista = lista;
        this.nombreEstudio = nombreEstudio;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public ArrayList<TipoDato> getLista(){
        return lista;
    }




    public AdaptadorTiposDato(@NonNull Context context) {
        super();
    }


}
