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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import de.fahrgemeinschaft.R;

public abstract class BaseImageButton extends FrameLayout {

    static final String android = "http://schemas.android.com/apk/res/android";

    public ImageButton icon;

    protected static int ID = Integer.MAX_VALUE;


    public BaseImageButton(Context ctx) {
        super(ctx);
        View.inflate(getContext(), inflate(), this);
        icon = (ImageButton) findViewById(R.id.icon);
        icon.setId(ID--);
    }

    protected abstract int inflate();

    public BaseImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(getContext(), inflate(), this);
        icon = (ImageButton) findViewById(R.id.icon);
        icon.setId(ID--);
        icon.setImageResource(attrs.getAttributeResourceValue(
                android, "src", R.drawable.ic_launcher));
        icon.setContentDescription(getContext().getString(
                attrs.getAttributeResourceValue(
                        android, "contentDescription",
                        attrs.getAttributeResourceValue(
                                android, "text", R.string.app_name))));
    }

    public void streifenhornchen(boolean on) {
        View streifen = findViewById(R.id.inactive);
        if (on) streifen.setVisibility(VISIBLE);
        else streifen.setVisibility(INVISIBLE);
        Util.fixStreifenhoernchen(streifen);
    }
}
