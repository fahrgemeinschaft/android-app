/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;

public class EditRideFragment3 extends SherlockFragment 
        implements OnClickListener {

    private static final String TAG = "Fahrgemeinschaft";

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_edit3, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
    }



    static class VisibilityView extends ImageButton implements OnClickListener {

        private int visibility_state = R.attr.state_visible_for_nobody;

        public VisibilityView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            visibility_state++; // simply next state
            if (visibility_state > R.attr.state_visible_upon_request)
                visibility_state = R.attr.state_visible_for_nobody;
            refreshDrawableState();
        }

        @Override
        public int[] onCreateDrawableState(int size) {
            final int[] drawableState = super.onCreateDrawableState(size + 1);
            mergeDrawableStates(drawableState, new int[] { visibility_state });
            return drawableState;
        }
    }
}
