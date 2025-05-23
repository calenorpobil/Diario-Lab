package com.merlita.diariolab.Adaptadores;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;


import com.merlita.diariolab.EditActivity;
import com.merlita.diariolab.Modelos.TipoDato;
import com.merlita.diariolab.Utils.EstudiosSQLiteHelper;
import com.merlita.diariolab.MainActivity;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.OcurrenciaActivity;
import com.merlita.diariolab.R;
import com.merlita.diariolab.VerActivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;

public class AdaptadorEstudios extends RecyclerView.Adapter<AdaptadorEstudios.MiContenedor> {

    private Context context;
    private ArrayList<Estudio> lista;




    SQLiteDatabase db;
    private final int DB_VERSION= MainActivity.DB_VERSION;


    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        ConstraintLayout main, botones;
        boolean visible = false;
        TextView tvTitulo, tvDescripcion, tvEmoji, tvCuenta;
        Button btNuevaOcurrencia, btBorrar, btEditar, btVer;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            main = (ConstraintLayout) itemView.findViewById(R.id.main);
            botones = (ConstraintLayout) itemView.findViewById(R.id.botones);

            tvTitulo = (TextView) itemView.findViewById(R.id.tvEstudioElegir);
            tvDescripcion = (TextView) itemView.findViewById(R.id.tvDescripcion);
            tvEmoji = (TextView) itemView.findViewById(R.id.tvEmoji1);
            tvCuenta = (TextView) itemView.findViewById(R.id.tvCuenta);
            btNuevaOcurrencia = (Button) itemView.findViewById(R.id.tvCheck);
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
        View v = inflador.inflate(R.layout.fila_estudio, parent, false);

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
        holder.tvCuenta.setText(getOcurrencia(estudio.getNombre())+"");
        holder.botones.setVisibility(GONE);


        //NUEVA OCURRENCIA
        holder.btNuevaOcurrencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), OcurrenciaActivity.class);
                i.putExtra("FECHA_OCURRENCIA", LocalDateTime.now());
                i.putExtra("ES_NUEVA", LocalDateTime.now());
                i.putExtra("FK_ESTUDIO", estudio.getNombre());
                int num = holder.getAbsoluteAdapterPosition();
                i.putExtra("INDEX", num);
                //view.getContext().startActivity(i);


                Activity origin = (Activity)context;
                origin.startActivityForResult(i, 3);


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


        holder.btBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estudio actual = lista.get(holder.getAbsoluteAdapterPosition());
                try (EstudiosSQLiteHelper usdbh =
                             new EstudiosSQLiteHelper(context,
                                     "DBEstudios", null, DB_VERSION);) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("⚠");
                    builder.setMessage("Este Estudio tiene datos asignados. " +
                            "¿Seguro que quieres borrarlos todos?");
                    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            db=usdbh.getWritableDatabase();

                            String nombreEstudio = actual.getNombre();
                            if (usdbh.borrarEstudio(actual, db) != -1 &&
                                    usdbh.borrarTiposDatos_PorFK(db, nombreEstudio) != -1 &&
                                    usdbh.borrarDatos_PorFK(db, nombreEstudio) != -1 &&
                                    usdbh.borrarOcurrencia_PorFK(db, nombreEstudio) != -1) {
                                lista.remove(actual);
                                MainActivity.actualizarLocal();
                            }else{
                                // BORRADO INCORRECTO
                            }
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();

                } catch (Exception e) {
                    Log.e("DB_ERROR", "Error en transacción: " + e.getMessage());
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
        holder.btVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estudio actual = lista.get(holder.getAbsoluteAdapterPosition());

                Intent i = new Intent(view.getContext(), VerActivity.class);
                i.putExtra("ESTUDIO", estudio);

                Activity origin = (Activity) context;
                origin.startActivityForResult(i, 4);

            }
        });
    }

    private int getOcurrencia(String estudios) {
        int res = -1;


        try(EstudiosSQLiteHelper usdbh =
                    new EstudiosSQLiteHelper(context,
                            "DBEstudios", null,  DB_VERSION);){
            SQLiteDatabase db;
            db = usdbh.getWritableDatabase();

            res = usdbh.getCuentaOcurrencias(db, estudios);

            db.close();
        }

        return res;
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

    public static class FragmentoFecha extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private DatePickerDialog.OnDateSetListener listener;

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }
        public static FragmentoFecha newInstance(DatePickerDialog.OnDateSetListener listener) {
            FragmentoFecha fragment = new FragmentoFecha();
            fragment.setListener(listener);
            return fragment;
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
        {
            final Calendar c = Calendar.getInstance();
            return new DatePickerDialog(
                    requireContext(),
                    (view, year, month, day) -> {
                        if (listener != null) {
                            listener.onDateSet(view, year, month, day);
                        }
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
        }


        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            if (listener != null) {
                listener.onDateSet(datePicker, year, month, day);
            }

        }

    }
}
