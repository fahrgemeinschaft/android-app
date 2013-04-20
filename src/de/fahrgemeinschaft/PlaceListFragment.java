package de.fahrgemeinschaft;

import de.fahrgemeinschaft.R;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceListFragment extends ListFragment 
				implements LoaderCallbacks<Cursor>, TextWatcher {
	
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
		
	    adapter = new CursorAdapter(getActivity(), null) {
	    	
	    	@Override
	    	public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
	    		return getActivity().getLayoutInflater()
	    				.inflate(R.layout.view_place_list_entry, parent, false);
	    	}
	    	
			@Override
			public void bindView(View view, Context arg1, Cursor place) {
				((TextView) view.findViewById(R.id.place_name)).setText(place.getString(1));
				((TextView) view.findViewById(R.id.place_address)).setText(place.getString(2));
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
			return new GPlaces.AutocompleteLoader(
					getActivity(), search_field.getText().toString());
		}
		return null;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor places) {
		adapter.swapCursor(places);
		wheel.setVisibility(View.GONE);
	}
	
	@Override
	public void onTextChanged(CharSequence t, int start, int before, int count) {
		wheel.setVisibility(View.VISIBLE);
		getActivity().getSupportLoaderManager().restartLoader(GPLACES, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long foo) {
		adapter.getCursor().moveToPosition(position);
		String id = adapter.getCursor().getString(3);
		String name = adapter.getCursor().getString(1);
		
		((OnPlacePickedListener) getActivity()).onPlacePicked(id, name);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
	}
	
	
	public interface OnPlacePickedListener {
		public void onPlacePicked(String geohash, String name);
	}

	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

}
