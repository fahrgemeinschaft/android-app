/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import de.fahrgemeinschaft.R;

public class EditTextImageButton extends BaseImageButton
                implements TextWatcher, OnClickListener {

    private static final String Q = "q";
    private static final String EMPTY = "";
    private static final String INPUT_TYPE = "inputType";
    private static final String HINT = "hint";
    public AutoCompletePicker text;
    private TextListener textListener;
    protected String key;

    @Override
    protected int inflate() {
        return R.layout.btn_edit_text;
    }

    public EditTextImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        text = (AutoCompletePicker) findViewById(R.id.text);
        text.setThreshold(1);
        text.setId(ID--);
        text.setHint(getContext().getString(attrs.getAttributeResourceValue(
                droid, HINT, R.string.app_name)));
        text.setInputType((attrs.getAttributeIntValue(
                droid, INPUT_TYPE, InputType.TYPE_CLASS_TEXT)));
        text.addTextChangedListener(this);
        Util.fixStreifenhoernchen(text);
        text.setSelectAllOnFocus(true);
        icon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        text.requestFocus();
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