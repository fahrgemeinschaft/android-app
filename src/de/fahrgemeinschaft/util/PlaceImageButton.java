/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import de.fahrgemeinschaft.R;

public class PlaceImageButton extends BaseImageButton {

    public Button btn;

    @Override
    protected int inflate() {
        return R.layout.btn_date_picker;
    }

    public PlaceImageButton(Context context) {
        super(context);
        btn = (Button) findViewById(R.id.text);
        btn.setId(ID--);
    }

    public PlaceImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        btn = (Button) findViewById(R.id.text);
        btn.setId(ID--);
        btn.setText(getContext().getString(attrs.getAttributeResourceValue(
                android, "text", R.string.app_name)));
    }
}
