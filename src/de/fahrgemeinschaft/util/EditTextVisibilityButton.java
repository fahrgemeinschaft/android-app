/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import de.fahrgemeinschaft.R;

public class EditTextVisibilityButton extends EditTextImageButton
    implements OnClickListener {

    static final String android = "http://schemas.android.com/apk/res/android";

    // index order must match @array/visibility in strings.xml
    public static final int ANYONE = 0;
    public static final int MEMBERS = 1;
    public static final int REQUEST = 2;
    public static final int NONE = 3;

    private int visibility;
    private int imageResId;


    public EditTextVisibilityButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImageResource(attrs.getAttributeResourceValue(
                android, "src", R.drawable.icn_dropdown));
        image.setOnClickListener(this);
    }

    public void setImageResource(int resourceId) {
        imageResId = resourceId;
        setVisibility(visibility);
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
        switch (visibility) {
        case ANYONE:
            text.setEnabled(true);
            drawIcons(R.drawable.icn_visibility_anyone);
            break;
        case MEMBERS:
            text.setEnabled(true);
            drawIcons(R.drawable.icn_visibility_members);
            break;
        case REQUEST:
            text.setEnabled(true);
            drawIcons(R.drawable.icn_visibility_request);
            break;
        case NONE:
            text.setEnabled(false);
            drawIcons(R.drawable.icn_visibility_none);
            break;
        }
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.visibility));
        builder.setItems(R.array.visibility,
                new DialogInterface.OnClickListener() {
            
            @Override   // must match @array/visibility in strings.xml
            public void onClick(DialogInterface dialog, int visibility) {
                setVisibility(visibility);
            }
        }).show();
    }

    private void drawIcons(int resId) {
        image.setImageDrawable(new LayerDrawable(new Drawable[] {
                getResources().getDrawable(imageResId),
                getResources().getDrawable(resId)
        }));
    }
}
