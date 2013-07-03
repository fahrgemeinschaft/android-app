/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

public abstract class EndlessSpinningZebraListFragment extends SherlockListFragment {

    abstract void bindListItemView(View view, Cursor cursor);

    private boolean spinning;
    private View wheel;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_list, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle savedInstanceState) {
        super.onViewCreated(layout, savedInstanceState);

        setListAdapter(new CursorAdapter(getActivity(), null, 0) {

            @Override
            public int getCount() {
                return super.getCount() + 1;
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position) {
                if (position < getCount() - 1)
                    return 0;
                else
                    return 1;
            }

            @Override
            public View getView(int pos, View v, ViewGroup parent) {
                if (pos < getCount() - 1)
                    v = super.getView(pos, v, parent);
                else {
                    if (v == null) {
                        v = getLayoutInflater(null).inflate(
                                R.layout.loading, parent, false);
                        wheel = v.findViewById(R.id.progress);
                        if (spinning) startSpinning();
                        else stopSpinning();
                    }
                }
                if (pos % 2 == 0) {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.medium_green));
                } else {
                    v.setBackgroundColor(getResources().getColor(
                            R.color.light_green));
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
    }


    public void startSpinning() {
        if (wheel != null) {
            final RotateAnimation rotateAnimation = new RotateAnimation(
                    0f, 360f, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(600);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            wheel.startAnimation(rotateAnimation);
        } else Toast.makeText(getActivity(), "NO wheel!", 3000).show();
        spinning = true;
    }

    public void stopSpinning() {
        if (wheel != null) wheel.clearAnimation();
        else Toast.makeText(getActivity(), "NO wheel!", 3000).show();
        spinning = false;
    }

    public interface ListFragmentCallback {
        public void onLoadFinished(Fragment fragment, Cursor cursor);
        public void onListItemClick(int position);
        public void onSpinningWheelClick();
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
}
