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
        email.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        land.setInputType(InputType.TYPE_CLASS_PHONE);
        mobile.setInputType(InputType.TYPE_CLASS_PHONE);
    }



    static class VisibilityView extends ImageButton implements OnClickListener {

        private int state = R.attr.state_visible_for_nobody; // default

        public VisibilityView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("foo");
            builder.setItems(R.array
                    .visibility, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    state = R.attr.state_visible_for_nobody + item;
                }
            }).show();
        }

        @Override
        public int[] onCreateDrawableState(int size) {
            final int[] drawableState = super.onCreateDrawableState(size + 1);
            mergeDrawableStates(drawableState, new int[] { state });
            return drawableState;
        }
    }
}
