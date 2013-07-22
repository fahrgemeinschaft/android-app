/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import de.fahrgemeinschaft.R;

public class ButtonImageButton extends FrameLayout {

    static final String android = "http://schemas.android.com/apk/res/android";

    public Button btn;
    public ImageButton icn;

    private static int ID = Integer.MAX_VALUE;


    public ButtonImageButton(Context ctx) {
        super(ctx);
        init();
    }

    public void init() {
        View.inflate(getContext(), R.layout.btn_button_image, this);
        icn = (ImageButton) findViewById(R.id.icon);
        btn = (Button) findViewById(R.id.text);
        btn.setId(ID--);
        icn.setId(ID--);
    }

    public ButtonImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        btn.setText(getContext().getString(attrs.getAttributeResourceValue(
                android, "text", R.string.app_name)));
        icn.setImageResource(attrs.getAttributeResourceValue(
                android, "src", R.drawable.ic_launcher));
        icn.setContentDescription(getContext().getString(
                attrs.getAttributeResourceValue(
                android, "contentDescription",
                attrs.getAttributeResourceValue(
                android, "text", R.string.app_name))));
    }
}
