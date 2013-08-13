/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.EditTextImageButton;
import de.fahrgemeinschaft.util.WebActivity;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileFragment extends SherlockFragment implements OnClickListener {

    private EditTextImageButton username;
    private EditTextImageButton password;
    private SharedPreferences prefs;


    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        getActivity().startService(
                new Intent(getActivity(), ConnectorService.class));
        return lI.inflate(R.layout.fragment_profile, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        username = (EditTextImageButton) v.findViewById(R.id.username);
        password = (EditTextImageButton) v.findViewById(R.id.password);
        v.findViewById(R.id.login).setOnClickListener(this);
        v.findViewById(R.id.register).setOnClickListener(this);
        super.onViewCreated(v, savedInstanceState);
        username.text.setText(prefs.getString("EMail", ""));
        password.text.setText(prefs.getString("password", ""));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        case R.id.login:
            prefs.edit()
                .putString("EMail", username.text.getText().toString())
                .putString("password", password.text.getText().toString())
                .remove("auth")
            .commit();
            Crouton.makeText(getActivity(), "Authentifiziere...", Style.INFO).show();
            ((InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(username.getWindowToken(), 0);
            ((NotificationManager) getActivity().getSystemService(
                    Context.NOTIFICATION_SERVICE)).cancel(42);
            getActivity().startService(
                    new Intent(getActivity(), ConnectorService.class)
                            .setAction(ConnectorService.AUTH));
            getActivity().getSupportFragmentManager().popBackStack();
            break;
        case R.id.register:
            getActivity().startActivity(
                    new Intent(getActivity(), WebActivity.class)
                    .setData(Uri.parse("file:///android_asset/register.html")));
            getActivity().overridePendingTransition(
                    R.anim.do_nix, R.anim.slide_in_bottom);
            break;
        }
    }

}
