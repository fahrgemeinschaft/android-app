/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;
import org.teleportr.RidesProvider;
import org.teleportr.ConnectorService.AuthListener;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.util.EditTextImageButton;
import de.fahrgemeinschaft.util.WebActivity;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileFragment extends SherlockFragment
        implements OnClickListener, ServiceConnection,
                OnSharedPreferenceChangeListener {

    private EditTextImageButton username;
    private EditTextImageButton password;
    private SharedPreferences prefs;
    private Button login;
    private ConnectorService server;


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
        login = (Button) v.findViewById(R.id.login);
        login.setOnClickListener(this);
        v.findViewById(R.id.register).setOnClickListener(this);
        super.onViewCreated(v, savedInstanceState);
        username.text.setText(prefs.getString("login",
                prefs.getString("Email", "")));
        password.text.setText(prefs.getString("password", ""));
        v.findViewById(R.id.container).setOnClickListener(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "auth");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("auth")) {
            if (prefs.contains("auth")) {
                login.setText(R.string.logout);
            } else {
                login.setText(R.string.login);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(
                new Intent(activity, ConnectorService.class), this, 0);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        case R.id.login:
            boolean logout = prefs.contains("auth");
            if (logout) {
                getActivity().getContentResolver().delete(
                        RidesProvider.getMyRidesUri(getActivity()), null, null);
                Crouton.makeText(getActivity(), getString(
                        R.string.logout), Style.CONFIRM).show();
            }
            Editor t = prefs.edit().remove("auth").remove("password")
                    .remove("user").remove("firstname").remove("lastname")
                    .putString("login", username.text.getText().toString());
            if (prefs.getBoolean("remember_password", false))
                t.putString("password", password.text.getText().toString());
            t.commit();
            if (!logout) { // i.e. login
                server.authenticate(password.text.getText().toString());
                ((InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(username.getWindowToken(), 0);
                ((NotificationManager) getActivity().getSystemService(
                        Context.NOTIFICATION_SERVICE)).cancel(42);
            }
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

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        server = ((ConnectorService.Bind) service).getService();
        server.authCallback((AuthListener) getActivity());
    }



    @Override
    public void onServiceDisconnected(ComponentName name) {}


}
