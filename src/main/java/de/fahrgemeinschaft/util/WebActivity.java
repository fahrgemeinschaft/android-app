/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft.util;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;

import de.fahrgemeinschaft.R;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
public class WebActivity extends SherlockActivity {

    private ProgressDialog progress;
    private WebView webView;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        progress = new ProgressDialog(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        webView.loadUrl(getIntent().getDataString());
        webView.requestFocus(View.FOCUS_DOWN);
        setContentView(webView);
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                    JsResult result) {
                Crouton.makeText(WebActivity.this, message, Style.ALERT).show();
                result.cancel();
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView v, String url, Bitmap favic) {
                if (url.startsWith("http")) {
                    progress.show();
                }
                super.onPageStarted(v, url, favic);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.dismiss();
                overridePendingTransition(
                        R.anim.do_nix, R.anim.slide_out_bottom);
                super.onPageFinished(view, url);
            }
        });
    }
}
