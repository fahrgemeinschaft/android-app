/**
 * Fahrgemeinschaft Ridesharing App
 *
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.Place;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlaceListFragment extends ListFragment implements
        LoaderCallbacks<Cursor>, TextWatcher {

    private static final int GPLACES = 42;
    private static final int LOCAL = 55;
    private EditText search_field;
    private CursorAdapter adapter;
    private Uri uri;
    private ProgressBar wheel;

    @Override
    public View onCreateView(LayoutInflater f, ViewGroup p, Bundle state) {
        return f.inflate(R.layout.fragment_place_list, p, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        search_field = (EditText) view.findViewById(R.id.autocomplete_text);
        wheel = (ProgressBar) view.findViewById(R.id.busy_search);

        adapter = new CursorAdapter(getActivity(), null, false) {

            @Override
            public View newView(Context arg0, Cursor arg1, ViewGroup p) {
                return getActivity().getLayoutInflater()
                        .inflate(R.layout.view_place_list_entry, p, false);
            }

            @Override
            public void bindView(View view, Context arg1, Cursor place) {
                ((TextView) view.findViewById(R.id.place_name))
                        .setText(place.getString(2));
                ((TextView) view.findViewById(R.id.place_address))
                        .setText(place.getString(3));
            }
        };
        setListAdapter(adapter);

        uri = getActivity().getIntent().getData();
        getActivity().getSupportLoaderManager().initLoader(LOCAL, null, this);
    }

    public void showSearchField() {
        search_field.setVisibility(View.VISIBLE);
        search_field.requestFocus();
        search_field.addTextChangedListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle b) {
        switch (id) {
        case LOCAL:
            return new CursorLoader(getActivity(), uri, null, null, null, null);
        case GPLACES:
            return new GPlaces.AutocompleteLoader(getActivity(),
                    search_field.getText().toString(), adapter.getCursor());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor places) {
        adapter.swapCursor(places);
        if (loader.getId() == LOCAL) {
            getActivity().getSupportLoaderManager()
                    .restartLoader(GPLACES, null, this);
        } else {
            wheel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTextChanged(CharSequence t, int start, int before, int count) {
        wheel.setVisibility(View.VISIBLE);
        getActivity().getSupportLoaderManager()
                .restartLoader(LOCAL, null, this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int a, int cnt, int b) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        adapter.getCursor().moveToPosition(position);
        uri = new Place().name(adapter.getCursor().getString(2))
                .address(adapter.getCursor().getString(3)).store(getActivity());

        ((PlacePickListener) getActivity()).onPlacePicked(uri);
    }

    public interface PlacePickListener {
        public void onPlacePicked(Uri uri);
    }

}
