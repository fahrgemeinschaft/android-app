/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import de.fahrgemeinschaft.R;

public class EditTextImageButton extends FrameLayout {

    static final String android = "http://schemas.android.com/apk/res/android";

    public EditText text;
    public ImageButton image;


    public EditTextImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(getContext(), R.layout.btn_edit_text_image, this);
        
        text = (EditText) findViewById(R.id.text);
        text.setHint(getContext().getString(attrs.getAttributeResourceValue(
                android, "hint", R.string.app_name)));
        text.setInputType((attrs.getAttributeIntValue(
                android, "inputType", InputType.TYPE_CLASS_TEXT)));
        
        image = (ImageButton) findViewById(R.id.icon);
        image.setImageResource(attrs.getAttributeResourceValue(
                android, "src", R.drawable.icn_dropdown));
        image.setContentDescription(getContext().getString(
                attrs.getAttributeResourceValue(
                android, "contentDescripion",
                attrs.getAttributeResourceValue(
                android, "hint", R.string.app_name))));
    }

}
