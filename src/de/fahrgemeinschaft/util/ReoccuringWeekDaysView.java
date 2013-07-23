package de.fahrgemeinschaft.util;

import java.text.DateFormatSymbols;

import org.json.JSONException;
import org.json.JSONObject;

import de.fahrgemeinschaft.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ReoccuringWeekDaysView extends LinearLayout {

    static final String android = "http://schemas.android.com/apk/res/android";

    public static final String[] DAYS = new String[] { "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    private JSONObject details;

    private boolean clickable;

    public ReoccuringWeekDaysView(Context context, AttributeSet attrs) {
        super(context, attrs);
        clickable = attrs.getAttributeBooleanValue(android, "clickable", true);
        String[] weekDays = new DateFormatSymbols().getShortWeekdays();
        for (int i = 2; i <= weekDays.length; i++) {
            TextView day = makeRecurringDayButton(getContext());
            day.setText(weekDays[i < weekDays.length? i : 1].substring(0, 2));
            addView(day);
        }
    }

    public void setDays(JSONObject details) {
        this.details = details;
        try {
            JSONObject days = details.getJSONObject("reoccur");
            for (int i = 0; i < 7; i++) {
                getChildAt(i).setSelected(days.getBoolean(DAYS[i]));
            }
        } catch (Exception e) {
            clear();
        }
    }

    public void clear() {
        for (int i = 0; i < 7; i++) getChildAt(i).setSelected(false);
    }

    private TextView makeRecurringDayButton(Context ctx) {
        TextView day = new TextView(ctx);
        if (clickable)
            day.setOnClickListener(toggleSelectedState);
        LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        day.setLayoutParams(lp);
        day.setTextAppearance(getContext(), R.style.dark_Bold);
        day.setBackgroundResource(R.drawable.btn_night);
        day.setGravity(Gravity.CENTER);
        return day;
    }

    OnClickListener toggleSelectedState = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            v.setSelected(!v.isSelected());
            updateDays();
        }
    };

    public void updateDays() {
        JSONObject days = new JSONObject();
        try {
            for (int i = 0; i < 7; i++) {
                days.put(DAYS[i], getChildAt(i).isSelected());
                System.out.println("update " + i + getChildAt(i).isSelected());
            }
            details.put("reoccur", days);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
