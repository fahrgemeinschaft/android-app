package de.fahrgemeinschaft.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.teleportr.Ride.COLUMNS;

import de.fahrgemeinschaft.FahrgemeinschaftConnector;
import de.fahrgemeinschaft.R;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RideRowView extends LinearLayout {

    private static final String EUR = "€";
    private static final String EMPTY = "";
    private static final int MINUTE = 60000;
    private static final long NOTIME = 59000;
    private static final SimpleDateFormat dayf =
            new SimpleDateFormat("EEE", Locale.GERMANY);
    private static final SimpleDateFormat datef =
            new SimpleDateFormat("dd.MM.yy", Locale.GERMANY);
    private static SimpleDateFormat timef =
            new SimpleDateFormat("HH:mm", Locale.GERMANY);

    ImageView seats;
    TextView price;
    TextView date;
    TextView time;
    TextView day;

    public RideRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(getContext(), R.layout.view_ride_row, this);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        seats = (ImageView) findViewById(R.id.seats);
        price = (TextView) findViewById(R.id.price);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        day = (TextView) findViewById(R.id.day);
    }

    public void bind(Cursor cursor, Context ctx) {
        long timestamp = cursor.getLong(COLUMNS.DEPARTURE);
        Date dep = new Date(timestamp);
        if (cursor.getInt(COLUMNS.TYPE) ==
                FahrgemeinschaftConnector.TYPE_OFFER_REOCCURING) {
            day.setText(R.string.reoccurring);
            date.setText(EMPTY);
        } else {
            day.setText(dayf.format(dep));
            date.setText(datef.format(dep));
        }
        if (timestamp % MINUTE == NOTIME) {
            time.setText(EMPTY);
        } else {
            time.setText(timef.format(dep));
        }
        if (cursor.getInt(COLUMNS.PRICE) != -1) {
            price.setVisibility(View.VISIBLE);
            price.setText(cursor.getInt(COLUMNS.PRICE) / 100 + EUR);
        } else {
            price.setVisibility(View.INVISIBLE);
        }
        switch(cursor.getInt(COLUMNS.SEATS)){
        case 0:
            seats.setImageResource(R.drawable.icn_seats_white_full); break;
        case 1:
            seats.setImageResource(R.drawable.icn_seats_white_1); break;
        case 2:
            seats.setImageResource(R.drawable.icn_seats_white_2); break;
        case 3:
            seats.setImageResource(R.drawable.icn_seats_white_3); break;
        default:
            seats.setImageResource(R.drawable.icn_seats_white_many); break;
        }
    }
}