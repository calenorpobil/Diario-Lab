package com.merlita.diariolab.Adaptadores;

import static android.view.View.VISIBLE;


import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.MainActivity;
import com.merlita.diariolab.Modelos.Analisis;
import com.merlita.diariolab.Modelos.Estudio;
import com.merlita.diariolab.Modelos.Pareja;
import com.merlita.diariolab.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdaptadorAnalisis extends RecyclerView.Adapter<AdaptadorAnalisis.MiContenedor> implements AdaptadorTabla.ItemClickListener {

    private static final int GRID_SIZE = 5;
    private final Activity activity;
    private Context context;
    private Estudio estudio1, estudio2;
    SQLiteDatabase db;
    private final int DB_VERSION= MainActivity.DB_VERSION;
    private ArrayList<Analisis> listaAnalisis;
    private Point p;
    int anchoPantalla, altoPantalla;
    private Analisis analisisActual;
    private int numFila=0, numColumna=0;
    private AdaptadorTabla adaptadorTabla;

    @Override
    public void onItemClick(View view, int position) {

    }


    public class MiContenedor extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        ConstraintLayout main;
        TextView tvEstudio2, tvEstudio1, tvEmoji1, tvEmoji2;
        RecyclerView rvGrafico;

        public MiContenedor(@NonNull View itemView) {
            super(itemView);

            main = (ConstraintLayout) itemView.findViewById(R.id.main);

            tvEstudio2 = (TextView) itemView.findViewById(R.id.tvEstudioElegir);
            tvEstudio1 = (TextView) itemView.findViewById(R.id.tvEstudio1);
            tvEmoji1 = (TextView) itemView.findViewById(R.id.tvEmoji1);
            tvEmoji2 = (TextView) itemView.findViewById(R.id.tvEmoji2);
            rvGrafico = (RecyclerView) itemView.findViewById(R.id.glGrafico);

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
        analisisActual = listaAnalisis.get(holder.getAbsoluteAdapterPosition());
        Estudio estudio1 = analisisActual.getEstudio1();
        Estudio estudio2 = analisisActual.getEstudio2();
        holder.tvEstudio2.setText(estudio2.getNombre());
        holder.tvEmoji2.setText(estudio2.getEmoji());
        holder.tvEstudio1.setText(estudio1.getNombre());
        holder.tvEmoji1.setText(estudio1.getEmoji());

        //mostrarGrid(holder);
        ArrayList<ArrayList<String>> data;

        data = analisisActual.getListaTabla();

        int numColumns = data.get(0).size();  // Número de columnas (incluye cabecera)

        GridLayoutManager layoutManager = new GridLayoutManager(context, numColumns);
        holder.rvGrafico.setLayoutManager(layoutManager);

        adaptadorTabla = new AdaptadorTabla(context, data, this.activity);
        adaptadorTabla.setClickListener(this);
        holder.rvGrafico.setAdapter(adaptadorTabla);


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
        int filas = analisisActual.getDatos1().size()+1, columnas = analisisActual.getDatos2().size()+1;
        int[] ids = new int[columnas*filas];


        LayoutParams lp =
                new LayoutParams(
                        (int) (anchoPantalla / (columnas*1.25)),
//                        ActionBar.LayoutParams.MATCH_PARENT,
                        altoPantalla / (filas*3));
//        holder.glGrafico.setRowCount(filas);
//        holder.glGrafico.setColumnCount(columnas);


        asignarCeldas(holder, filas, columnas, lp, ids);
    }

    private void asignarCeldas(@NonNull MiContenedor holder,
                               int filas, int columnas, LayoutParams lp, int[] ids) {
        HashMap<Pareja<String, String>, Integer> resulDatos = analisisActual.getResulDatos();
        ArrayList<Pareja<String, String>> parejas = analisisActual.getParejas();
        ArrayList<String> valoresFilas = new ArrayList<>();
        ArrayList<String> valoresColumnas = new ArrayList<>();
        for (int i = 0; i < filas * columnas; i++) {
            colocarBotones(holder, filas, columnas, lp, ids, i,
                    resulDatos, parejas, valoresFilas, valoresColumnas);
        }
    }

    private void colocarBotones(@NonNull MiContenedor holder, int filas, int columnas,
                                LayoutParams lp, int[] ids, int i,
                                HashMap<Pareja<String, String>, Integer> resulDatos,
                                ArrayList<Pareja<String, String>> parejas,
                                ArrayList<String> valoresFilas, ArrayList<String> valoresColumnas) {


        TextView b = new TextView(this.context);
        View circle = new View(this.context);
        circle.setBackgroundResource(R.drawable.circle_shape);

        b.setLayoutParams(lp);
        b.setGravity(Gravity.FILL_HORIZONTAL);
        // Poner nombres de columnas
        if (i >0 && i < columnas) {
            ids[i] = ViewGroup.generateViewId();
            numColumna = i-1;
            String texto = analisisActual.getDatos1().get( numColumna ).getValorText();
            valoresColumnas.add(texto);
            b.setText(texto);
            b.setId(ids[i]);
            holder.rvGrafico.addView(b);

        // Poner nombres de filas
        } else if (i !=0 && i % filas == 0){
            ids[i] = ViewGroup.generateViewId();
            numFila = (i / filas)-1;
            String texto = analisisActual.getDatos2().get( numFila ).getValorText();
            valoresFilas.add(texto);
            b.setText(texto);
            b.setId(ids[i]);
            b.setVisibility(VISIBLE);
            holder.rvGrafico.addView(b);

        // Poner valores
        } else {
            if(i!=0){
                numColumna = i-(columnas*(numFila+1))-1;
                String datoFila = valoresFilas.get( numFila );
                String datoColumna = valoresColumnas.get( numColumna );
                Pareja<String, String> parejaTabla = new Pareja<>(datoFila, datoColumna);
                int max = analisisActual.getRepesMax();
                int ancho = (int) (anchoPantalla / (columnas*1.50));
                int veces = resulDatos.getOrDefault( parejaTabla, 0 );
                int porcentaje = 0;
                if(veces!=0) porcentaje = veces/max;
                int anchura = ancho*porcentaje;
                lp.width = anchura;
                lp.height = anchura;
                ids[i] = ViewGroup.generateViewId();
                circle.setId(ids[i]);
            }
//                holder.glGrafico.setUseDefaultMargins(false);
            circle.setLayoutParams(lp);
            holder.rvGrafico.addView(circle);
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
        this.listaAnalisis = lista;
    }

    @Override
    public int getItemCount() {
        return listaAnalisis.size();
    }

}
