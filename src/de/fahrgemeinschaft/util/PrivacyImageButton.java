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

public class PrivacyImageButton extends EditTextImageButton
                implements OnClickListener {

    static final String android = "http://schemas.android.com/apk/res/android";


    private int privacy;
    private int imageResId;
    private PrivacyListener privacyListener;


    public PrivacyImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImageResource(attrs.getAttributeResourceValue(
                android, SRC, R.drawable.icn_dropdown));
        icon.setOnClickListener(this);
    }

    public void setImageResource(int resourceId) {
        imageResId = resourceId;
        setVisibility(privacy);
    }

    public int getPrivacy() {
        return privacy;
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
        switch (privacy) {
        case 1:
            text.setEnabled(true);
            drawIcons(R.drawable.icn_visibility_anyone);
            break;
        case 4:
            text.setEnabled(true);
            drawIcons(R.drawable.icn_visibility_members);
            break;
        case 0:
            text.setEnabled(true);
            drawIcons(R.drawable.icn_visibility_request);
            break;
        case 5:
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
            public void onClick(DialogInterface dialog, int click_idx) {
                switch (click_idx) {
                case 0: // anyone
                    setPrivacy(1);
                    break;
                case 1: // members
                    setPrivacy(4);
                    break;
                case 2: // request
                    setPrivacy(0);
                    break;
                case 3: // none
                    setPrivacy(5);
                    break;
                }
                if (privacyListener != null)
                    privacyListener.onPrivacyChange(key, privacy);
            }
        }).show();
    }

    private void drawIcons(int resId) {
        icon.setImageDrawable(new LayerDrawable(new Drawable[] {
                getResources().getDrawable(imageResId),
                getResources().getDrawable(resId)
        }));
    }

    public void setPrivacyListener(String key, PrivacyListener listener) {
        this.privacyListener = listener;
        this.key = key;
    }

    public interface PrivacyListener {
        public void onPrivacyChange(String key, int visibility);
    }
}