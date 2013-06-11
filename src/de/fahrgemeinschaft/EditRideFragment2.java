/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment2 extends SherlockFragment 
        implements OnClickListener, OnDateSetListener {

    private static final String TAG = "Fahrgemeinschaft";

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit2, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        v.findViewById(R.id.btn_pick_date).setOnClickListener(this);
        v.findViewById(R.id.ic_pick_date).setOnClickListener(this);
    }

    public void showDatePickerDialog(View v) {
    }

    @Override
    public void onClick(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog d = new DatePickerDialog(
                getActivity(), this, year, month, day);
        d.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ready),
                (android.content.DialogInterface.OnClickListener) null); //java!
        d.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        
    }
}
