/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment3 extends SherlockFragment {


    private EditText email;
    private EditText land;
    private EditText mobile;
    private EditText plate;
    private EditText name;



    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit3, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        email = (EditText) v.findViewById(R.id.email).findViewById(R.id.text);
        land = (EditText) v.findViewById(R.id.landline).findViewById(R.id.text);
        mobile = (EditText) v.findViewById(R.id.mobile).findViewById(R.id.text);
        plate = (EditText) v.findViewById(R.id.plate).findViewById(R.id.text);
        name = (EditText) v.findViewById(R.id.name).findViewById(R.id.text);
        email.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        land.setInputType(InputType.TYPE_CLASS_PHONE);
        mobile.setInputType(InputType.TYPE_CLASS_PHONE);
        
        ((VisibilityView) v.findViewById(R.id.email).findViewById(R.id.icon))
            .setImageResource(R.drawable.icn_contact_email);
        ((VisibilityView) v.findViewById(R.id.landline).findViewById(R.id.icon))
            .setImageResource(R.drawable.icn_contact_phone);
        ((VisibilityView) v.findViewById(R.id.mobile).findViewById(R.id.icon))
            .setImageResource(R.drawable.icn_contact_handy);
        ((VisibilityView) v.findViewById(R.id.plate).findViewById(R.id.icon))
            .setImageResource(R.drawable.icn_contact_kfz);
        ((VisibilityView) v.findViewById(R.id.name).findViewById(R.id.icon))
        .setImageResource(R.drawable.icn_view_user);
        email.setHint(getString(R.string.email_hint));
        land.setHint(getString(R.string.landline_hint));
        mobile.setHint(getString(R.string.mobile_hint));
        plate.setHint(getString(R.string.plate_hint));
        name.setHint(getString(R.string.name_hint));
    }

    static class VisibilityView extends ImageButton implements OnClickListener {

        // index order must match @array/visibility in strings.xml
        public static final int ANYONE = 0;
        public static final int MEMBERS = 1;
        public static final int REQUEST = 2;
        public static final int NONE = 3;
        private int visibility;
        private int icon;

        public VisibilityView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOnClickListener(this);
        }

        public int getVisibility() {
            return visibility;
        }

        @Override
        public void setImageResource(int resId) {
            icon = resId;
            setVisibility(ANYONE);
        }

        public void setVisibility(int visibility) {
            switch (visibility) {
            case ANYONE:
                setIcon(R.drawable.icn_visibility_anyone);
                break;
            case MEMBERS:
                setIcon(R.drawable.icn_visibility_members);
                break;
            case REQUEST:
                setIcon(R.drawable.icn_visibility_request);
                break;
            case NONE:
                setIcon(R.drawable.icn_visibility_none);
                break;
            }
        }

        private void setIcon(int resId) {
            setImageDrawable(new LayerDrawable(new Drawable[] {
                    getResources().getDrawable(icon),
                    getResources().getDrawable(resId)
            }));
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getResources().getString(R.string.visibility));
            builder.setItems(R.array
                    .visibility, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int visibility) {
                    setVisibility(visibility);
                }
            }).show();
        }
    }
}
