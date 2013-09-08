/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.fahrgemeinschaft.R;

public abstract class SpinningZebraListFragment extends SherlockListFragment {

    abstract public void bindListItemView(View view, Cursor cursor);

    protected boolean spinningEnabled = true;

    public boolean isSpinningEnabled() {
        return spinningEnabled;
    }

    public void setSpinningEnabled(boolean spinningEnabled) {
        this.spinningEnabled = spinningEnabled;
    }

    private RotateAnimation rotate;
    protected View spinner;
    public boolean onScreen;
    private String smallText;
    private String largeText;
    private boolean spinning;
    protected Cursor cursor;


    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_list, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);
        layout.setOnClickListener(null);
        setListAdapter(new CursorAdapter(getActivity(), null, 0) {

            @Override
            public int getCount() {
                if (spinningEnabled)
                    return super.getCount() + 1;
                else return super.getCount();
            }

            @Override
            public int getViewTypeCount() {
                if (spinningEnabled) return 2;
                else return 1;
            }

            @Override
            public int getItemViewType(int position) {
                if (!spinningEnabled || position < getCount() - 1)
                    return 0;
                else
                    return 1;
            }

            @Override
            public View getView(int position, View v, ViewGroup parent) {
                if (!spinningEnabled || position < getCount() - 1)
                    v = super.getView(position, v, parent);
                else {
                    if (v == null) {
                        v = getLayoutInflater(null).inflate(
                                R.layout.view_spinning_wheel, parent, false);
                    }
                    ((TextView) v.findViewById(R.id.small)).setText(smallText);
                    ((TextView) v.findViewById(R.id.large)).setText(largeText);
                    if (spinning && onScreen) {
                        v.findViewById(R.id.progress).startAnimation(rotate);
                    } else if (onScreen) {
                        v.findViewById(R.id.progress).clearAnimation();
                    }
                }
                if (position % 2 == 0) {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.medium_green));
                } else {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.almost_medium_green));
                }
                return v;
            }
            
            @Override
            public View newView(Context ctx, Cursor rides, ViewGroup parent) {
                return getLayoutInflater(null).inflate(
                        R.layout.view_ride_list_entry, parent, false);
            }
            
            @Override
            public void bindView(View view, Context ctx, Cursor ride) {
                if (ride.getPosition() == ride.getCount()) return;
                bindListItemView(view, ride);
            }
        });
        registerForContextMenu(getListView());
        rotate = new RotateAnimation(
                0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(600);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setRepeatCount(Animation.INFINITE);
        stopSpinning("click here");
        swapCursor(cursor);
        getListView().requestFocus();
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        if (getListAdapter() != null) {
            ((CursorAdapter) getListAdapter()).swapCursor(cursor);
        }
    }

    public void startSpinning(String smallText, String largeText) {
        this.smallText = smallText;
        this.largeText = largeText;
        spinning = true;
        if (cursor != null && !cursor.isClosed() && getListAdapter() != null) {
            ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }
    
    public void stopSpinning(String smallText) {
        this.smallText = smallText;
        this.largeText = "";
        spinning = false;
        if (cursor != null && !cursor.isClosed() && getListAdapter() != null) {
            ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = ((CursorAdapter) getListView().getAdapter()).getCursor();
        if (position != c.getCount()) {
            ((ListFragmentCallback) getActivity()).onListItemClick(position);
        } else {
            ((ListFragmentCallback) getActivity()).onSpinningWheelClick();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        onScreen = true;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        onScreen = false;
        super.onDetach();
    }

    public interface ListFragmentCallback {
        public void onListItemClick(int position);
        public void onSpinningWheelClick();
    }
}
