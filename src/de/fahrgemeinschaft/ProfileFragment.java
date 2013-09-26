/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;
import org.teleportr.RidesProvider;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

import de.fahrgemeinschaft.ContactProvider.CONTACT;
import de.fahrgemeinschaft.util.EditTextImageButton;
import de.fahrgemeinschaft.util.WebActivity;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileFragment extends SherlockFragment
        implements OnClickListener, OnSharedPreferenceChangeListener {

    private EditTextImageButton username;
    private EditTextImageButton password;
    private SharedPreferences prefs;
    private Button login;
    private Button register;


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
        register = (Button) v.findViewById(R.id.register);
        register.setOnClickListener(this);
        super.onViewCreated(v, savedInstanceState);
        username.text.setText(prefs.getString("login",
                prefs.getString("Email", "")));
        password.text.setText(prefs.getString("password", ""));
        v.findViewById(R.id.container).setOnClickListener(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "auth");
        login.requestFocus();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("auth")) {
            if (prefs.contains("auth")) {
                login.setText(R.string.logout);
//                login.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.btn_logout, 0, 0);
                password.setVisibility(View.GONE);
                register.setVisibility(View.GONE);
            } else {
                login.setText(R.string.login);
                password.setVisibility(View.VISIBLE);
                register.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((NotificationManager) getActivity().getSystemService(
                Context.NOTIFICATION_SERVICE)).cancel(42);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        case R.id.login:
            boolean logout = prefs.contains("auth");
            if (logout) {
                Crouton.makeText(getActivity(), getString(
                        R.string.logout), Style.CONFIRM).show();
                getActivity().getContentResolver().delete(
                        RidesProvider.getMyRidesUri(getActivity()), null, null);
                getActivity().getContentResolver().update(RidesProvider
                        .getRidesUri(getActivity()), null, null, null);
                getActivity().getContentResolver().delete(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/"
                        + prefs.getString(CONTACT.USER, null)),
                null, null);
                getActivity().startService(
                        new Intent(getActivity(), ConnectorService.class)
                        .setAction(ConnectorService.SEARCH));
            }
            Editor t = prefs.edit().remove("auth").remove("password")
                    .remove(CONTACT.USER).remove("firstname").remove("lastname")
                    .putString("login", username.text.getText().toString());
            if (prefs.getBoolean("remember_password", false))
                t.putString("password", password.text.getText().toString());
            t.commit();
            ((BaseActivity) getActivity()).setProfileIcon();
            boolean login = !logout;
            if (login) { // i.e. login
                prefs.edit().putBoolean("init_contacts", true).commit();
                ((BaseActivity)getActivity()).service
                        .authenticate(password.text.getText().toString());
                ((InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(username.getWindowToken(), 0);
                ((NotificationManager) getActivity().getSystemService(
                        Context.NOTIFICATION_SERVICE)).cancel(42);
                getActivity().getContentResolver().update(RidesProvider
                        .getRidesUri(getActivity()), null, null, null);
            }
            getActivity().getSupportFragmentManager().popBackStack();
            break;
        case R.id.register:
            getActivity().startActivity(
                    new Intent(getActivity(), WebActivity.class)
                    .setData(Uri.parse("http://www.fahrgemeinschaft.de/register_mobile.php")));
            getActivity().overridePendingTransition(
                    R.anim.do_nix, R.anim.slide_in_bottom);
            break;
        }
    }

}
