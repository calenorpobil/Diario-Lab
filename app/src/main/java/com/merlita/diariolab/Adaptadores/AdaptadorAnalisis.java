package com.merlita.diariolab.Adaptadores;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.MainActivity;
import com.merlita.diariolab.Modelos.Analisis;
import com.merlita.diariolab.Modelos.CircleItem;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Pareja;
import com.merlita.diariolab.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdaptadorAnalisis extends RecyclerView.Adapter<AdaptadorAnalisis.MiContenedor> {

    private static final int GRID_SIZE = 5;
    private final Activity activity;
    private Context context;
    private Estudio estudio1, estudio2;
    SQLiteDatabase db;
    private final int DB_VERSION= MainActivity.DB_VERSION;
    private ArrayList<Analisis> lista;
    private Point p;
    int anchoPantalla, altoPantalla;
    private Analisis actual;
    private int cuentaDatos = 0;

    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        ConstraintLayout main;
        TextView tvEstudio2, tvEstudio1, tvEmoji1, tvEmoji2;
        GridLayout glGrafico;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            main = (ConstraintLayout) itemView.findViewById(R.id.main);

            tvEstudio2 = (TextView) itemView.findViewById(R.id.tvEstudio2);
            tvEstudio1 = (TextView) itemView.findViewById(R.id.tvEstudio1);
            tvEmoji1 = (TextView) itemView.findViewById(R.id.tvEmoji1);
            tvEmoji2 = (TextView) itemView.findViewById(R.id.tvEmoji2);
            glGrafico = (GridLayout) itemView.findViewById(R.id.glGrafico);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenu.ContextMenuInfo contextMenuInfo)
        {
            contextMenu.add(getAbsoluteAdapterPosition(), 121, 0, "EDITAR");
            contextMenu.add(getAbsoluteAdapterPosition(), 122, 1, "BORRAR");
        }
    }


    //PONER VALORES
    @Override
    public void onBindViewHolder(@NonNull MiContenedor holder, int position) {
        actual = lista.get(holder.getAbsoluteAdapterPosition());
        Estudio estudio1 = actual.getEstudio1();
        Estudio estudio2 = actual.getEstudio2();
        holder.tvEstudio2.setText(estudio2.getNombre());
        holder.tvEmoji2.setText(estudio2.getEmoji());
        holder.tvEstudio1.setText(estudio1.getNombre());
        holder.tvEmoji1.setText(estudio1.getEmoji());

        mostrarGrid(holder);


        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private void mostrarGrid(@NonNull MiContenedor holder) {
        p = new Point();
        Display pantallaDisplay = activity.getWindowManager().getDefaultDisplay();
        pantallaDisplay.getSize(p);
        anchoPantalla = p.x;
        altoPantalla = p.y;
        // Añado 1 para colocar los nombres de fila y columna:
        int filas = actual.getDatos1().size()+1, columnas = actual.getDatos2().size()+1;
        int[] ids = new int[columnas*filas];


        ViewGroup.LayoutParams lp =
                new ViewGroup.LayoutParams(
                        (int) (anchoPantalla / (columnas*1.25)),
//                        ActionBar.LayoutParams.WRAP_CONTENT,
                        altoPantalla / (filas*3));
        holder.glGrafico.setRowCount(filas);
        holder.glGrafico.setColumnCount(columnas);

        asignarCeldas(holder, filas, columnas, lp, ids);
    }

    private void asignarCeldas(@NonNull MiContenedor holder,
                               int filas, int columnas, ViewGroup.LayoutParams lp, int[] ids) {
        HashMap<Pareja<String, String>, Integer> resulDatos = actual.getResulDatos();
        ArrayList<Pareja<String, String>> parejas = actual.getParejas();
        for (int i = 0; i < filas * columnas; i++) {
            colocarBotones(holder, filas, columnas, lp, ids, i,
                    resulDatos, parejas);
        }
    }

    private void colocarBotones(@NonNull MiContenedor holder, int filas, int columnas,
                                ViewGroup.LayoutParams lp, int[] ids, int i,
                                HashMap<Pareja<String, String>, Integer> resulDatos,
                                ArrayList<Pareja<String, String>> parejas) {
        TextView b = new TextView(this.context);
        View circle = new View(this.context);
        circle.setBackgroundResource(R.drawable.circle_shape);

        b.setLayoutParams(lp);
        b.setGravity(Gravity.FILL_HORIZONTAL);
        // Poner nombres de columnas
        if (i >0 && i < columnas) {
            ids[i] = ViewGroup.generateViewId();
            String texto = actual.getDatos1().get(i - 1).getValorText();
            b.setText(texto);
            b.setId(ids[i]);
            holder.glGrafico.addView(b);

        // Poner nombres de filas
        } else if (i !=0 && i % filas == 0){
            ids[i] = ViewGroup.generateViewId();
            int numDato = filas / i;
            String texto = actual.getDatos2().get(numDato).getValorText();
            b.setText(texto);
            b.setId(ids[i]);
            holder.glGrafico.addView(b);

        // Poner valores
        } else {
            int numfila = 0, numColumna = 0;
            // TODO: Averiguar qué pareja indica cada celda.
            Pareja<String, String> parejaTabla = parejas.get(0);
            int anchura = resulDatos.get(parejaTabla);
            lp.width = anchura;
            lp.height = anchura;
            ids[i] = ViewGroup.generateViewId();
            circle.setId(ids[i]);
//                holder.glGrafico.setUseDefaultMargins(false);
            circle.setLayoutParams(lp);
            holder.glGrafico.addView(circle);
        }
    }


    @NonNull
    @Override
    public MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflador =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflador.inflate(R.layout.fila_analisis, parent, false);

        return new MiContenedor(v);
    }


    public AdaptadorAnalisis(Context context, Activity activity,
                             ArrayList<Analisis> lista) {
        super();
        this.activity = activity;
        this.context = context;
        this.lista = lista;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

}
