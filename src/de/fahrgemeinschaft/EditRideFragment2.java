/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.teleportr.Ride;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment2 extends SherlockFragment 
        implements OnClickListener, OnDateSetListener, OnTimeSetListener {

    private static final String TAG = "Fahrgemeinschaft";
    private LinearLayout recurrence;
    private Button date_button;
    private Button time_button;
    private EditText price;
	private Ride ride; 

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit2, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        v.findViewById(R.id.btn_pick_date).setOnClickListener(this);
        v.findViewById(R.id.ic_pick_date).setOnClickListener(this);
        v.findViewById(R.id.btn_pick_time).setOnClickListener(this);
        v.findViewById(R.id.ic_pick_time).setOnClickListener(this);
        
        date_button = (Button) v.findViewById(R.id.btn_pick_date);
        time_button = (Button) v.findViewById(R.id.btn_pick_time);
        price = (EditText) v.findViewById(R.id.btn_pick_price);
        price.setOnFocusChangeListener(new OnFocusChangeListener() {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    price.setText("");
                } else {
                    String p = price.getText().toString();
                    if (p.matches("\\d+\\.?\\d*"))
                        setPrice((int) Double.valueOf(p).doubleValue() * 100);
                    else
                        setPrice(ride.getPrice());
                }
            }
        });
        
        String[] weekDays = new DateFormatSymbols().getShortWeekdays();
        recurrence = (LinearLayout) v.findViewById(R.id.recurrence);
        LayoutParams layoutParams = dayButtonlayoutParams();
        for (int i = 1; i < weekDays.length; i++) {
            TextView day = makeRecurringDayButton(getActivity());
            day.setText(weekDays[i].substring(0, 2));
            recurrence.addView(day);
        }
    }

    public void setRide(Ride ride) {
        this.ride = ride;
        setPrice(ride.getPrice());
        setDeparture(ride.getDep());
    }

    protected void setPrice(int p) {
        if (p != 0) {
            price.setText((p / 100) + " €");
        } else {
            price.setText("");
        }
        ride.price(p);
    }
    
    @Override
    public void onClick(View v) {
        final Calendar c = Calendar.getInstance();
        switch (v.getId()) {
        case R.id.btn_pick_date:
        case R.id.ic_pick_date:
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog d = new DatePickerDialog(
                    getActivity(), this, year, month, day);
            d.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ready),
                    (android.content.DialogInterface.OnClickListener) null); //java!
            d.show();
            break;
        case R.id.btn_pick_time:
        case R.id.ic_pick_time:
            int min = c.get(Calendar.MINUTE);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            TimePickerDialog t = new TimePickerDialog(
                    getActivity(), this, hour, min, true);
            t.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ready),
                    (android.content.DialogInterface.OnClickListener) null); //java!
            t.show();
            break;

        default:
            break;
        }
    }

    private void setDeparture(long timestamp) {
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTimeInMillis(timestamp);
        if (cal.get(Calendar.DAY_OF_YEAR) == today)
            date_button.setText(getString(R.string.today));
        else if (cal.get(Calendar.DAY_OF_YEAR) == today + 1)
            date_button.setText(getString(R.string.tomorrow));
        else if (cal.get(Calendar.DAY_OF_YEAR) == today + 2)
            date_button.setText(getString(R.string.after_tomorrow));
        else
            date_button.setText(new SimpleDateFormat("dd. MMM yyyy",
                    Locale.GERMANY).format(timestamp));
        time_button.setText(new SimpleDateFormat("HH:mm",
                Locale.GERMANY).format(timestamp));
        ride.dep(timestamp);
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ride.getDep());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        setDeparture(cal.getTime().getTime());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ride.getDep());
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        setDeparture(cal.getTime().getTime());
    }



    private TextView makeRecurringDayButton(Context ctx) {
        TextView day = new TextView(ctx);
        day.setOnClickListener(toggleSelectedState);
        day.setLayoutParams(dayButtonlayoutParams());
        day.setTextAppearance(getActivity(), R.style.dark_Bold);
        day.setBackgroundResource(R.drawable.btn_day);
        day.setGravity(Gravity.CENTER);
        return day;
    }

    OnClickListener toggleSelectedState = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            v.setSelected(!v.isSelected());
        }
    };

    public void setDateButtonText(long timestamp) {
    }
    
    private LayoutParams dayButtonlayoutParams() {
        LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        int margin = getActivity().getResources() // dips
                .getDimensionPixelSize(R.dimen.small);
        lp.leftMargin = margin;
        lp.rightMargin = margin;
        lp.topMargin = margin;
        lp.bottomMargin = margin;
        lp.weight = 1;
        return lp;
    }

}
