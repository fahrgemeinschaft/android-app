package de.fahrgemeinschaft;


import java.util.Date;

import org.teleportr.ConnectorService;
import org.teleportr.Place;
import org.teleportr.Ride;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.fahrgemeinschaft.R;

import de.fahrgemeinschaft.RangeSeekBar.OnRangeSeekBarChangeListener;



public class MainActivity extends Activity implements OnClickListener {

    protected static final String TAG = "fahrgemeinschaft";
	private static final int FROM = 42;
	private static final int TO = 55;
	private Button from_btn;
	private Button to_btn;
	private String from_id = "u33d6435rzx";
	protected String to_id;
	private View selberfahren_btn;
	private View mitfahren_btn;
	private ProgressBar wheel;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
//        FahrgemeinschaftsConnector c = new FahrgemeinschaftsConnector();
//        GeoHash g = GeoHash.fromGeohashString("abcd");
        
        RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(20, 75, this);
        seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
                @Override
                public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                        // handle changed range values
                        Log.i(TAG, "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
                }
        });

        // add RangeSeekBar to pre-defined layout
        ViewGroup layout = (ViewGroup) findViewById(R.id.layout);
//        layout.addView(seekBar);
        
        from_btn = (Button) findViewById(R.id.btn_autocomplete_from);
        to_btn = (Button) findViewById(R.id.btn_autocomplete_to);
        selberfahren_btn = findViewById(R.id.btn_selberfahren);
        mitfahren_btn = findViewById(R.id.btn_mitfahren);

        to_btn.setOnClickListener(this);
        from_btn.setOnClickListener(this);
        mitfahren_btn.setOnClickListener(this);
        selberfahren_btn.setOnClickListener(this);
        findViewById(R.id.btn_pick_to).setOnClickListener(this);
        findViewById(R.id.btn_pick_from).setOnClickListener(this);
        
        if (savedInstanceState != null) {
        	from_id = savedInstanceState.getString("from_id");
        	to_id = savedInstanceState.getString("to_id");
        }
       
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_pick_from:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					Uri.parse("content://de.fahrgemeinschaft/places/")), FROM);
			overridePendingTransition(R.anim.slide_in_left, R.anim.do_nothing);
			break;
			
		case R.id.btn_pick_to:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					Uri.parse("content://de.fahrgemeinschaft/places?from=" + from_id )), TO);
			overridePendingTransition(R.anim.slide_in_right, R.anim.do_nothing);
			break;
		
		case R.id.btn_autocomplete_from:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					Uri.parse("content://de.fahrgemeinschaft/places/"))
					.putExtra("show_textfield", true), FROM);
			overridePendingTransition(R.anim.slide_in_left, R.anim.do_nothing);
			break;
		
		case R.id.btn_autocomplete_to:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					Uri.parse("content://de.fahrgemeinschaft/places/"))
					.putExtra("show_textfield", true), TO);
			overridePendingTransition(R.anim.slide_in_right, R.anim.do_nothing);
			break;
			
		case R.id.btn_mitfahren:
			new Ride()
 				.type(Ride.SEARCH)
 				.from(from_id)
 				.to(to_id)
 				.dep(new Date());
			//Toast.makeText(this, "yay", 200).show();
			Ride.saveAll(this);
			startService(new Intent(this, ConnectorService.class));
			startActivity(new Intent(this, ResultsActivity.class));
			break;
			
		default:
			break;
		}
	}
 
	@Override
	protected void onActivityResult(final int req, int res, final Intent intent) {
		
		if (res == RESULT_OK) {
			switch (req) {
			case FROM:
				animatePulse(from_btn);
				from_id = intent.getData().getLastPathSegment();
				from_btn.setText(intent.getStringExtra("name"));
				from_btn.setTextAppearance(this, R.style.white_button_text);
				break;
			case TO:
				animatePulse(to_btn);
				to_id = intent.getData().getLastPathSegment();
				to_btn.setText(intent.getStringExtra("name"));
				to_btn.setTextAppearance(this, R.style.white_button_text);
				break;
			}
			if (intent.getData().getScheme().equals("https")) {
				resolveGPlace(req, intent);
			}
		}
	}

	private void resolveGPlace(final int request, final Intent intent) {
		mitfahren_btn.setEnabled(false);
		selberfahren_btn.setEnabled(false);
		if (request == FROM)
			wheel = (ProgressBar) findViewById(R.id.busy_from);
		else
			wheel = (ProgressBar) findViewById(R.id.busy_to);
		wheel.setVisibility(View.VISIBLE);
		
		new GPlaces.DetailsTask(){ 
			protected void onPostExecute(Place result) {
				
				result.name(intent.getStringExtra("name"));
				if (request == FROM)
					from_id = result.geohash;
				else
					to_id = result.geohash;
				Ride.saveAll(MainActivity.this);

				selberfahren_btn.setEnabled(true);
				mitfahren_btn.setEnabled(true);
				wheel.setVisibility(View.INVISIBLE);
			};
		}.execute(intent.getData());
	}
	
	private void animatePulse(final View view) {
		Animation fade_in = new AlphaAnimation(0.3f, 1f);
		fade_in.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) { 
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) { }
			
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setBackgroundResource(android.R.color.white);
				Animation fade_out = new AlphaAnimation(1f, 0.7f);
				fade_out.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) { 
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) { }
					
					@Override
					public void onAnimationEnd(Animation animation) {
						view.setBackgroundResource(R.drawable.btn_white);
					
					}
				});
				fade_out.setDuration(1400);
				view.startAnimation(fade_out);
			}
		});
		fade_in.setDuration(190);
		view.startAnimation(fade_in);
	}
	
   
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putString("from_id", from_id);
    	outState.putString("to_id", to_id);
    }
    
    
}
