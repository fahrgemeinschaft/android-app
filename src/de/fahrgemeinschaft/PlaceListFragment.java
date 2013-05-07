/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;
import org.teleportr.Place;

import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlaceListFragment extends ListFragment
        implements LoaderCallbacks<Cursor>, TextWatcher {

    private static final int GPLACES = 42;
    private static final int LOCAL = 55;
    private static final String TAG = "PlaceList";
    private EditText search_field;
    private CursorAdapter adapter;
    private Uri uri;
    private ProgressBar wheel;
    private ImageButton toggle;

    @Override
    public View onCreateView(LayoutInflater f, ViewGroup p, Bundle state) {
        return f.inflate(R.layout.fragment_place_list, p, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        search_field = (EditText) view.findViewById(R.id.autocomplete_text);
        wheel = (ProgressBar) view.findViewById(R.id.busy_search);
        toggle = (ImageButton) view.findViewById(R.id.toggle);
        search_field.addTextChangedListener(this);
        toggle.setOnClickListener(
                new OnClickListener() {
                    
                    @Override
                    public void onClick(View arg0) {
                        toggleSearchField();
                    }
                });

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

    public void toggleSearchField() {
        if (search_field.getVisibility() == View.GONE) {
            search_field.setVisibility(View.VISIBLE);
            search_field.requestFocus();
            toggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            search_field.setVisibility(View.GONE);
            toggle.setImageResource(android.R.drawable.ic_menu_search);
        }
        ((InputMethodManager) getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE)).toggleSoftInput(0, 0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle b) {
        String text = search_field.getText().toString();
        switch (id) {
        case LOCAL:
            String from_id = uri.getQueryParameter("from_id");
            uri = uri.buildUpon().encodedQuery(
                    ((from_id != null)? "from_id=" +from_id : "")
                    + "&q=" + text).build();
            System.out.println(uri);
            return new CursorLoader(getActivity(), uri, null, null, null, null);
        case GPLACES:
            return new GPlaces.AutocompleteLoader(getActivity(),
                    text, adapter.getCursor());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor places) {
        adapter.swapCursor(places);
        if (loader.getId() == LOCAL && search_field.getVisibility() == 0) {
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
        if (adapter.getCursor().getColumnCount() == 5) {
            uri = new Place()
                .name(adapter.getCursor().getString(2))
                .address(adapter.getCursor().getString(3))
                .set("gplace:id", adapter.getCursor().getString(4))
                .store(getActivity());
            Log.d(TAG, "picked new gPlace: " + uri);
            getActivity().startService(
                    new Intent(getActivity(), ConnectorService.class)
                        .setAction(ConnectorService.RESOLVE));
        } else {
            uri = Uri.parse("content://" + getActivity().getPackageName()
                    + "/places/"+ adapter.getCursor().getLong(0));
            Log.d(TAG, "picked: " + uri);
        }
        ((PlacePickListener) getActivity()).onPlacePicked(uri);
    }

    public interface PlacePickListener {
        public void onPlacePicked(Uri uri);
    }

}
