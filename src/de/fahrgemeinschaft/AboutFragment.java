package de.fahrgemeinschaft;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class AboutFragment extends SherlockFragment implements OnClickListener {

    @Override
    public View onCreateView(final LayoutInflater lI, ViewGroup p, Bundle b) {
        return lI.inflate(R.layout.fragment_about, p, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        v.findViewById(R.id.logo).setOnClickListener(this);
        v.findViewById(R.id.layout).setOnClickListener(this);
        v.findViewById(R.id.github).setOnClickListener(this);
        v.findViewById(R.id.version).setOnClickListener(this);
        v.findViewById(R.id.disclaimer).setOnClickListener(this);
        v.findViewById(R.id.attribution).setOnClickListener(this);
        try {
            ((TextView)v.findViewById(R.id.version)).setText(
                    getActivity().getPackageManager().getPackageInfo(
                            getActivity().getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.logo:
            openBrowser("http://fahrgemeinschaft.de");
            break;
        case R.id.layout:
            getActivity().getSupportFragmentManager().popBackStack();
            break;
        case R.id.github:
        case R.id.version:
            openBrowser("http://github.com/fahrgemeinschaft/android-app");
            break;
        case R.id.disclaimer:
            openBrowser("https://gnu.org/licenses/gpl.html");
            break;
        case R.id.attribution:
            openBrowser("http://actionbarsherlock.com/");
            break;
        }
    }

    private void openBrowser(String url) {
        getActivity().startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}