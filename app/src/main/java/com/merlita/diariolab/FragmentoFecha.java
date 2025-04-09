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

    /*
    private FragmentoFechaViewModel mViewModel;

    public static FragmentoFecha newInstance() {
        return new FragmentoFecha();
    }*/

/*
    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        //TODO: hs = (FragmentoHora.HoraSeleccionada)
    }
    public void onTimeSet(Time time, int i, int i1){

    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);

    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        // Convert the date elements to strings.
        // Set the activity to the Main Activity.
        MainActivity activity = (MainActivity) getActivity();
        // Invoke Main Activity's processDatePickerResult() method.
        processDatePickerResult(year, month, day);
    /*
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FragmentoFechaViewModel.class);
        // TODO: Use the ViewModel
    }*/
    }
    public void processDatePickerResult(int year, int month, int day) {
        // The month integer returned by the date picker starts counting at 0
        // for January, so you need to add 1 to show months starting at 1.
        String month_string = Integer.toString(month + 1);
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);
        // Assign the concatenated strings to dateMessage.
        String dateMessage = (month_string + "/" + day_string + "/" + year_string);

        //tv.setText(dateMessage);
    }

}
