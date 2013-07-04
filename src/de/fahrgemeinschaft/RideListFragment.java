/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.teleportr.Ride.COLUMNS;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RideListFragment extends EndlessSpinningZebraListFragment {

    private static final SimpleDateFormat day =
            new SimpleDateFormat("EEE", Locale.GERMANY);
    private static final SimpleDateFormat date =
            new SimpleDateFormat("dd.MM.", Locale.GERMANY);
    private static SimpleDateFormat time =
            new SimpleDateFormat("HH:mm", Locale.GERMANY);
    private String[] split;

    @Override
    void bindListItemView(View view, Cursor ride) {
        RideView v = (RideView) view;
        v.from_place.setText(ride.getString(COLUMNS.FROM_NAME));
        split = ride.getString(COLUMNS.FROM_ADDRESS).split(",");
        if (split.length > 1)
            v.from_city.setText(split[1]);
        else
            v.from_city.setText(ride.getString(COLUMNS.FROM_ADDRESS));

        v.to_place.setText(ride.getString(COLUMNS.TO_NAME));
        split = ride.getString(COLUMNS.TO_ADDRESS).split(",");
        if (split.length > 1)
            v.to_city.setText(split[1]);
        else
            v.to_city.setText(ride.getString(COLUMNS.TO_ADDRESS));

        Date timestamp = new Date(ride.getLong(COLUMNS.DEPARTURE));
        v.day.setText(day.format(timestamp));
        v.date.setText(date.format(timestamp));
        v.time.setText(time.format(timestamp));

        v.price.setText(ride.getInt(COLUMNS.PRICE) / 100 + "");
        switch(ride.getInt(COLUMNS.SEATS)){
        case 0:
            v.seats.setImageResource(R.drawable.icn_seats_white_full); break;
        case 1:
            v.seats.setImageResource(R.drawable.icn_seats_white_1); break;
        case 2:
            v.seats.setImageResource(R.drawable.icn_seats_white_2); break;
        case 3:
            v.seats.setImageResource(R.drawable.icn_seats_white_3); break;
        default:
            v.seats.setImageResource(R.drawable.icn_seats_white_many); break;
        }
    }





    static class RideView extends RelativeLayout {

        TextView from_city;
        TextView from_place;
        TextView to_city;
        TextView to_place;
        ImageView seats;
        TextView price;
        TextView day;
        TextView date;
        TextView time;

        public RideView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            from_city = (TextView) findViewById(R.id.from_city);
            from_place = (TextView) findViewById(R.id.from_place);
            to_city = (TextView) findViewById(R.id.to_city);
            to_place = (TextView) findViewById(R.id.to_place);
            seats = (ImageView) findViewById(R.id.seats_icon);
            price = (TextView) findViewById(R.id.price);
            day = (TextView) findViewById(R.id.day);
            date = (TextView) findViewById(R.id.date);
            time = (TextView) findViewById(R.id.time);
        }
    }



}
