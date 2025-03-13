package com.merlita.diariolab;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Estudio;

import java.util.ArrayList;

public class AdaptadorTiposDato extends RecyclerView.Adapter<AdaptadorTiposDato.MiContenedor> {

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



    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        ConstraintLayout main, botones;
        boolean visible = false;
        TextView tvTitulo, tvDescripcion, tvEmoji, tvCuenta;
        Button btMas;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            main = (ConstraintLayout) itemView.findViewById(R.id.main);
            botones = (ConstraintLayout) itemView.findViewById(R.id.botones);

            tvTitulo = (TextView) itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = (TextView) itemView.findViewById(R.id.tvDescripcion);
            tvEmoji = (TextView) itemView.findViewById(R.id.tvEmoji);
            tvCuenta = (TextView) itemView.findViewById(R.id.tvCuenta);
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
        holder.tvDescripcion.setText(estudio.getDescripcion());
        holder.tvEmoji.setText(estudio.getEmoji());
        holder.botones.setVisibility(GONE);


        holder.btMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estudio actual = lista.get(holder.getAdapterPosition());

            }
        });
        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!holder.visible){
                    holder.botones.setVisibility(VISIBLE);
                    holder.visible=true;
                }else{
                    holder.botones.setVisibility(GONE);
                    holder.visible=false;
                }
            }
        });

    }


    public AdaptadorTiposDato(Context context, ArrayList<Estudio> lista,
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



    public AdaptadorTiposDato(@NonNull Context context) {
        super();
    }


}
