package com.merlita.diariolab.Adaptadores;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.merlita.diariolab.Modelos.Analisis;
import com.merlita.diariolab.Modelos.CircleItem;
import com.merlita.diariolab.R;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorGridAnalisis extends BaseAdapter {
    private final Activity context;
    private final ArrayList<CircleItem> lista;
    private int selectedPosition = -1;

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public CircleItem getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    public static class ViewHolder
    {
        public View txtViewTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        LayoutInflater inflator = context.getLayoutInflater();

        if(convertView==null)
        {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.gridview_row, null);

            view.txtViewTitle = (View) convertView.findViewById(R.id.circle_view);

            convertView.setTag(view);


        }
        else
        {
            view = (ViewHolder) convertView.getTag();
        }


        return convertView;
    }

    public AdaptadorGridAnalisis(Activity context,
                                 ArrayList<CircleItem> lista){
        super();
        this.context = context;
        this.lista = lista;

    }
}