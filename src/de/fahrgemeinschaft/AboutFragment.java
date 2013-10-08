/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class AboutFragment extends SherlockFragment implements OnClickListener {

    private TextView title;

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_about, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        v.findViewById(R.id.layout).setOnClickListener(this);
        String text = "fahrgemeinschaft.de <font color=#FFFFFF><b>APP</b></font>";
        title = (TextView) v.findViewById(R.id.title);
        title.setText(Html.fromHtml(text));
        v.findViewById(R.id.version).setOnClickListener(this);
        v.findViewById(R.id.license_text).setOnClickListener(this);
        v.findViewById(R.id.crouton).setOnClickListener(this);
        v.findViewById(R.id.actionbarsherlock).setOnClickListener(this);
        v.findViewById(R.id.geohash).setOnClickListener(this);
        v.findViewById(R.id.teleportr).setOnClickListener(this);
        v.findViewById(R.id.volley).setOnClickListener(this);
        v.findViewById(R.id.sonnenstreifen_logo).setOnClickListener(this);
        v.findViewById(R.id.subphisticated_logo).setOnClickListener(this);
        v.findViewById(R.id.iconmonstr).setOnClickListener(this);
        ((TextView) v.findViewById(R.id.iconmonstr)).setText(Html.fromHtml(
                "most icons by<font color=#D0E987> iconmonstr</font>"));
        try {
            ((TextView)v.findViewById(R.id.version)).setText( "Version " +
                    getActivity().getPackageManager().getPackageInfo(
                            getActivity().getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        v.findViewById(R.id.git_url).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.layout:
            getActivity().getSupportFragmentManager().popBackStack();
            break;
        case R.id.git_url:
        case R.id.version:
            openBrowser("http://github.com/fahrgemeinschaft/android-app");
            break;
        case R.id.license_text:
            openBrowser("https://gnu.org/licenses/gpl.html");
            break;
        case R.id.crouton:
            openBrowser("https://github.com/keyboardsurfer/Crouton");
            break;
        case R.id.actionbarsherlock:
            openBrowser("http://actionbarsherlock.com");
            break;
        case R.id.geohash:
            openBrowser("https://github.com/kungfoo/geohash-java");
            break;
        case R.id.teleportr:
            openBrowser("https://github.com/teleportR/android-library");
            break;
        case R.id.volley:
            openBrowser(
                "https://android.googlesource.com/platform/frameworks/volley");
            break;
        case R.id.sonnenstreifen_logo:
            sendMail("fg@sonnenstreifen.de");
            break;
        case R.id.iconmonstr:
            openBrowser("http://iconmonstr.com");
            break;
        case R.id.subphisticated_logo:
            sendMail("fg@subphisticated.com");
            break;
        }
    }

    private void openBrowser(String url) {
        getActivity().startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void sendMail(String adress) {
        getActivity().startActivity(
                new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+ adress)));
    }
}