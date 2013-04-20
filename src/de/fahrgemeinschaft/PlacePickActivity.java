package de.fahrgemeinschaft;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import de.fahrgemeinschaft.R;

import de.fahrgemeinschaft.PlaceListFragment.OnPlacePickedListener;

public class PlacePickActivity extends FragmentActivity implements OnPlacePickedListener {

	private PlaceListFragment place_list;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		setContentView(R.layout.activity_place_pick);

		place_list = (PlaceListFragment) 
				getSupportFragmentManager().findFragmentById(R.id.place_list);

		if (getIntent().getBooleanExtra("show_textfield", false)) {
			place_list.showSearchField();
		}
	}

	
	
	@Override
	public void onPlacePicked(String id, String name) {
		
		if (id.length() > 21) {
			// obviously a google place
			setResult(RESULT_OK, new Intent("", 
					Uri.parse("https://maps.googleapis.com/maps/api/place" +
							"/details/json?sensor=true&reference=" + id))
					.putExtra("name", name));
		} else {
			setResult(RESULT_OK, new Intent("", 
					Uri.withAppendedPath(getIntent().getData(), id))
					.putExtra("name", name));
		}

		finish();
		if (getIntent().getData().getQueryParameter("from") == null)
			overridePendingTransition(R.anim.do_nothing, R.anim.slide_out_left);
		else
			overridePendingTransition(R.anim.do_nothing, R.anim.slide_out_right);
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
		if (getIntent().getData().getQueryParameter("from") == null)
			overridePendingTransition(R.anim.do_nothing, R.anim.slide_out_left);
		else
			overridePendingTransition(R.anim.do_nothing, R.anim.slide_out_right);
		super.onBackPressed();
	}
}
