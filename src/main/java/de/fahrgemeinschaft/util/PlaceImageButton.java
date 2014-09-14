/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft.util;

import org.teleportr.Place;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;
import de.fahrgemeinschaft.R;

public class PlaceImageButton extends BaseImageButton {

    private TextView address;
    public Button name;

    @Override
    protected int inflate() {
        return R.layout.btn_place_picker;
    }

    public PlaceImageButton(Context context) {
        super(context);
        name = (Button) findViewById(R.id.name);
        name.setId(ID--);
        address = (TextView) findViewById(R.id.address);
        address.setId(ID--);
    }

    public PlaceImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        name = (Button) findViewById(R.id.name);
        name.setId(ID--);
        name.setText(getContext().getString(attrs.getAttributeResourceValue(
                droid, TEXT, R.string.app_name)));
        address = (TextView) findViewById(R.id.address);
        address.setId(ID--);
    }

    public void setPlace(Place place) {
        if (place != null) {
            name.setText(place.getName());
            address.setText(place.getAddress());
        }
    }
}