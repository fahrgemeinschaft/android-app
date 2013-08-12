/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import de.fahrgemeinschaft.R;

public class EditTextImageButton extends FrameLayout implements TextWatcher {

    static final String android = "http://schemas.android.com/apk/res/android";

    public EditText text;
    protected String key;
    public ImageButton image;
    private TextListener textListener;
    private static int ID = Integer.MAX_VALUE / 2;



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

        text.addTextChangedListener(this);
        image.setId(ID--);
        text.setId(ID--);
        Util.fixBackgroundRepeat(text);
    }

    public interface TextListener {
        public void onTextChange(String key, String text);
    }

    public void setTextListener(String key, TextListener listener) {
        this.textListener = listener;
        this.key = key;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (textListener != null)
            textListener.onTextChange(key, text.getText().toString());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int cnt, int a) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int cnt) {}
}
