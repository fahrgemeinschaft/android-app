package de.fahrgemeinschaft.util;

import android.content.Context;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class AutoCompletePicker extends AutoCompleteTextView
        implements OnFocusChangeListener {

    public AutoCompletePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnFocusChangeListener(this);
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) performCompletion();
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }
}
