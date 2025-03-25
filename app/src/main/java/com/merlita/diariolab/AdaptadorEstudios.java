package com.merlita.diariolab;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.TipoDato;

import java.util.ArrayList;

public class AdaptadorEstudios extends RecyclerView.Adapter<AdaptadorEstudios.MiContenedor> {

    private Context context;
    private ArrayList<Estudio> lista;




    SQLiteDatabase db;
    private final int DB_VERSION=MainActivity.DB_VERSION;


    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        ConstraintLayout main, botones;
        boolean visible = false;
        TextView tvTitulo, tvDescripcion, tvEmoji, tvCuenta;
        Button btMas, btBorrar, btEditar, btVer;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            main = (ConstraintLayout) itemView.findViewById(R.id.main);
            botones = (ConstraintLayout) itemView.findViewById(R.id.botones);

            tvTitulo = (TextView) itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = (TextView) itemView.findViewById(R.id.tvDescripcion);
            tvEmoji = (TextView) itemView.findViewById(R.id.tvEmoji);
            tvCuenta = (TextView) itemView.findViewById(R.id.tvCuenta);
            btMas = (Button) itemView.findViewById(R.id.btMas);
            btBorrar = (Button) itemView.findViewById(R.id.btBorrar);
            btEditar = (Button) itemView.findViewById(R.id.btEditar);
            btVer = (Button) itemView.findViewById(R.id.btVer);
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
        int reverseIndex = lista.size() -1 -position;
        Estudio estudio = lista.get(holder.getAbsoluteAdapterPosition());
        holder.tvTitulo.setText(estudio.getNombre());
        holder.tvDescripcion.setText(estudio.getDescripcion());
        holder.tvEmoji.setText(estudio.getEmoji());
        holder.botones.setVisibility(GONE);


        holder.btMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estudio actual = lista.get(holder.getAbsoluteAdapterPosition());

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

        holder.btEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), EditActivity.class);
                i.putExtra("NOMBRE", estudio.getNombre());
                i.putExtra("EMOJI", estudio.getEmoji());
                i.putExtra("DESCRIPCION", estudio.getDescripcion());
                int num = holder.getAbsoluteAdapterPosition();
                i.putExtra("INDEX", num);
                //view.getContext().startActivity(i);


                Activity origin = (Activity)context;
                origin.startActivityForResult(i, 1);



            }
        });


        holder.btBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estudio actual = lista.get(holder.getAbsoluteAdapterPosition());
                try (EstudiosSQLiteHelper usdbh =
                             new EstudiosSQLiteHelper(context,
                                     "DBEstudios", null, DB_VERSION);) {

                    db=usdbh.getWritableDatabase();

                    if (usdbh.borrarEstudio(actual, db) != -1 &&
                            usdbh.borrarTiposDatos_PorFK(db, actual.getNombre())!=-1) {
                        lista.remove(actual);
                        MainActivity.actualizarLocal();

                    }

                } catch (Exception e) {
                    Log.e("DB_ERROR", "Error en transacción: " + e.getMessage());
                } finally {
                    // Finalizar la transacción
                    db.close();
                }
            }
        });
    }



    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult");
    }


    public AdaptadorEstudios(Context context, ArrayList<Estudio> lista) {
        super();
        this.context = context;
        this.lista = lista;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
