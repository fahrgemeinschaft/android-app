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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

public abstract class SpinningZebraListFragment
            extends SherlockListFragment
            implements LoaderCallbacks<Cursor> {

    private static final String ID = "id";
    private static final String URI = "uri";

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
    private int code;
    private Uri uri;


    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_ride_list, p, false);
    }

    @Override
    public void onViewCreated(View layout, Bundle state) {
        super.onViewCreated(layout, state);
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
        if (state != null) {
            code = state.getInt(ID);
            uri = (Uri) (state.getParcelable(URI));
            System.out.println("init loader");
            getActivity().getSupportLoaderManager()
                    .initLoader(code, state, this);
        } else if (uri != null) {
            System.out.println("restart loader");
            getActivity().getSupportLoaderManager()
                    .restartLoader(code, state, this);
        }
        getListView().requestFocus();
    }

    public void load(Uri uri, int id) {
        this.code = id;
        this.uri = uri;
        if (getActivity() != null) {
            System.out.println("NEVER");
            getActivity().getSupportLoaderManager()
                .restartLoader(getId(), null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle b) {
        System.out.println("create loader for " + uri);
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor rides) {
        if (getListAdapter() != null) {
            ((CursorAdapter) getListAdapter()).swapCursor(rides);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (getListAdapter() != null) {
            ((CursorAdapter) getListAdapter()).swapCursor(null);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(URI, uri);
        outState.putInt(ID, code);
    }

    public void startSpinning(String smallText, String largeText) {
        this.smallText = smallText;
        this.largeText = largeText;
        spinning = true;
        notifyDatasetChanged();
    }

    public void stopSpinning(String smallText) {
        this.smallText = smallText;
        this.largeText = "";
        spinning = false;
        notifyDatasetChanged();
    }

    public void notifyDatasetChanged() {
        if (getCursor() != null && !getCursor().isClosed()
                && getListAdapter() != null) {
            ((CursorAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    public Cursor getCursor() {
        if (getListView() != null) {
            return ((CursorAdapter) getListView().getAdapter()).getCursor();
        } else return null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        if (pos != getCursor().getCount()) {
            ((ListFragmentCallback) getActivity()).onListItemClick(pos, code);
        } else {
            ((ListFragmentCallback) getActivity()).onSpinningWheelClick();
        }
        super.onListItemClick(l, v, pos, id);
    }

    public interface ListFragmentCallback {
        public void onListItemClick(int position, int id);
        public void onSpinningWheelClick();
    }
}
