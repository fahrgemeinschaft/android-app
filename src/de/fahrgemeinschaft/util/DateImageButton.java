/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import de.fahrgemeinschaft.R;

public class DateImageButton extends BaseImageButton {

    public Button btn;

    @Override
    protected int inflate() {
        return R.layout.btn_date_picker;
    }

    public DateImageButton(Context context) {
        super(context);
        btn = (Button) findViewById(R.id.text);
        btn.setId(ID--);
    }

    public DateImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        btn = (Button) findViewById(R.id.text);
        btn.setId(ID--);
        btn.setText(getContext().getString(attrs.getAttributeResourceValue(
                android, "text", R.string.app_name)));
    }

    public void setDate(long timestamp) {
        Calendar cal = Calendar.getInstance();
        int thisYear = cal.get(Calendar.YEAR);
        int today = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTimeInMillis(timestamp);
        btn.setText(new SimpleDateFormat("dd. MMM yyyy",
                Locale.GERMANY).format(timestamp));
        if (cal.get(Calendar.YEAR) == thisYear) {
            if (cal.get(Calendar.DAY_OF_YEAR) == today)
                btn.setText(getContext().getString(R.string.today));
            else if (cal.get(Calendar.DAY_OF_YEAR) == today + 1)
                btn.setText(getContext().getString(R.string.tomorrow));
            else if (cal.get(Calendar.DAY_OF_YEAR) == today + 2)
                btn.setText(getContext().getString(R.string.after_tomorrow));
        }
    }

    public void setTime(long timestamp) {
        btn.setText(new SimpleDateFormat("HH:mm",
                Locale.GERMANY).format(timestamp));
    }
}
