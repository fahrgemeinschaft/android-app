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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import de.fahrgemeinschaft.R;

public class EditTextImageButton extends BaseImageButton
                implements TextWatcher, OnFocusChangeListener, OnClickListener {

    public EditText text;
    protected String key;
    private TextListener textListener;

    @Override
    protected int inflate() {
        return R.layout.btn_edit_text;
    }

    public EditTextImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        text = (EditText) findViewById(R.id.text);
        text.setId(ID--);
        text.setHint(getContext().getString(attrs.getAttributeResourceValue(
                android, "hint", R.string.app_name)));
        text.setInputType((attrs.getAttributeIntValue(
                android, "inputType", InputType.TYPE_CLASS_TEXT)));
        text.addTextChangedListener(this);
        Util.fixStreifenhoernchen(text);
        text.setSelectAllOnFocus(true);
        text.setOnFocusChangeListener(this);
        icon.setOnFocusChangeListener(this);
        icon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        text.requestFocus();
    }
    

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            text.requestFocus();
        } else {
            ((InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(text.getWindowToken(), 0);
        }
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
