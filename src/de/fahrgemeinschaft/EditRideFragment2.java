/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.util.Calendar;

import org.teleportr.Ride;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.DateImageButton;
import de.fahrgemeinschaft.util.EditTextImageButton;
import de.fahrgemeinschaft.util.EditTextImageButton.TextListener;
import de.fahrgemeinschaft.util.ReoccuringWeekDaysView;

public class EditRideFragment2 extends SherlockFragment 
        implements OnDateSetListener, OnTimeSetListener, TextListener {

    private static final String IS_VALID_PRICE_REGEX = "\\d+\\.?\\d*";
    private static final String EMPTY = "";
    private static final String EUR = " €";
    private ReoccuringWeekDaysView reoccur;
    private EditTextImageButton price;
    private DateImageButton date;
    private DateImageButton time;
    private View white_bg;
    private Ride ride; 

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit2, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        date = (DateImageButton) v.findViewById(R.id.date);
        white_bg = v.findViewById(R.id.white_bg);
        date.btn.setOnClickListener(pickDate);
        date.icon.setOnClickListener(pickDate);
        time = (DateImageButton) v.findViewById(R.id.time);
        time.btn.setOnClickListener(pickTime);
        time.icon.setOnClickListener(pickTime);
        price = (EditTextImageButton) v.findViewById(R.id.price);
        price.text.setOnFocusChangeListener(onPriceChange);
        price.setTextListener(Ride.PRICE, this);
        reoccur = (ReoccuringWeekDaysView) v.findViewById(R.id.reoccur);
        time.requestFocus();
    }

    public void setRide(Ride ride) {
        this.ride = ride;
        setPrice(ride.getPrice());
        reoccur.setDays(ride.getDetails());
        if (reoccur.isReoccuring()) {
            date.icon.setEnabled(false);
            date.btn.setEnabled(false);
            date.streifenhornchen(true);
            white_bg.setVisibility(View.VISIBLE);
            date.btn.setText(R.string.reccurence_date);
            if (ride.getDep() > System.currentTimeMillis()) {
                ride.dep(System.currentTimeMillis());
            }
            ride.type(FahrgemeinschaftConnector.TYPE_OFFER_REOCCURING);
        } else {
            ride.type(Ride.OFFER);
            date.icon.setEnabled(true);
            date.btn.setEnabled(true);
            date.streifenhornchen(false);
            white_bg.setVisibility(View.GONE);
            date.setDate(ride.getDep());
            time.setTime(ride.getDep());
        }
    }

    protected void setPrice(int p) {
        if (p != 0) {
            price.text.setText((p / 100) + EUR);
        } else {
            price.text.setText(EMPTY);
        }
        ride.price(p);
    }

    @Override
    public void onTextChange(String key, String text) {
        if (text.matches(IS_VALID_PRICE_REGEX))
            ride.price((int) Double.valueOf(text).doubleValue() * 100);
    }

    OnFocusChangeListener onPriceChange = new OnFocusChangeListener() {
        
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                String text = price.text.getText().toString();
                if (text.length() > 2)
                    price.text.setText(text.substring(0, text.length() - 2));
                ((EditText) v).selectAll();
            } else {
                String p = price.text.getText().toString();
                if (p.matches(IS_VALID_PRICE_REGEX))
                    setPrice((int) Double.valueOf(p).doubleValue() * 100);
                else
                    setPrice(ride.getPrice());
            }
        }
    };

    OnClickListener pickDate = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            openDatePicker();
        }
    };

    public void openDatePicker() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ride.getDep());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog d = new DatePickerDialog(getActivity(),
                EditRideFragment2.this, year, month, day);
        d.show();
    }

    OnClickListener pickTime = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(ride.getDep());
            int min = c.get(Calendar.MINUTE);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            new TimePickerDialog(getActivity(),
                    EditRideFragment2.this, hour, min, true).show();
        }
    };

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ride.getDep());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        date.setDate(cal.getTimeInMillis());
        ride.dep(cal.getTimeInMillis());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ride.getDep());
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        time.setTime(cal.getTimeInMillis());
        ride.dep(cal.getTimeInMillis());
    }
}
