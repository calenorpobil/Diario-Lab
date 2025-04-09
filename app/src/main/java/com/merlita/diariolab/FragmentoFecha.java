package com.merlita.diariolab;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;


import java.sql.Time;
import java.util.Calendar;


public class FragmentoFecha extends DialogFragment implements DatePickerDialog.OnDateSetListener {

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
