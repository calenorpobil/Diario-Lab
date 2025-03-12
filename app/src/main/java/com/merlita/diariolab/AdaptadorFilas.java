package com.merlita.diariolab;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.merlita.diariolab.Modelos.Estudio;

import java.util.ArrayList;

public class AdaptadorFilas extends RecyclerView.Adapter<AdaptadorFilas.MiContenedor> {

    private Context context;
    private ArrayList<Estudio> lista;
    private boolean viendoDatosPrueba=true;
    private static boolean usando = false;
    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    private OnButtonClickListener listener;
    Estudio estudioFila;




    SQLiteDatabase db;





    @NonNull
    @Override
    public MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflador =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflador.inflate(R.layout.text_row_item, parent, false);

        return new MiContenedor(v);
    }

    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        Estudio estudio = lista.get(holder.getAdapterPosition());
        holder.tvTitulo.setText(estudio.getNombre());
        holder.tvAutor.setText(estudio.getDescripcion());


        holder.btEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estudio actual = lista.get(holder.getAdapterPosition());

            }
        });
        holder.btMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estudio actual = lista.get(holder.getAdapterPosition());

            }
        });

    }


    public AdaptadorFilas(Context context, ArrayList<Estudio> lista,
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



    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        TextView tvTitulo, tvAutor, tvCuenta;
        Button btEmoji, btMas;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);


            tvTitulo = (TextView) itemView.findViewById(R.id.tvTitulo);
            tvAutor = (TextView) itemView.findViewById(R.id.tvDescripcion);
            tvCuenta = (TextView) itemView.findViewById(R.id.tvCuenta);
            btEmoji = (Button) itemView.findViewById(R.id.btEmoji);
            btMas = (Button) itemView.findViewById(R.id.btMas);
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


    public void filtrarLista() {
        if(viendoDatosPrueba){
            lista.remove(0);
            lista.remove(0);
            lista.remove(0);
            viendoDatosPrueba=false;
        }
        notifyDataSetChanged();
    }



    public AdaptadorFilas(@NonNull Context context) {
        super();
    }


}
