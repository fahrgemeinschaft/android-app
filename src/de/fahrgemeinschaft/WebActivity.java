/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;

public class WebActivity extends SherlockActivity {

    private static final String TAG = "Fahrgemeinschaft";
    private ProgressDialog progress;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        progress = new ProgressDialog(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/register.html");
        webView.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageStarted(WebView v, String url, Bitmap favic) {
                Log.d(TAG, "url");
                if (url.startsWith("http")) {
                    Log.d(TAG, "url http");
                    progress.show();
                }
                super.onPageStarted(v, url, favic);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "finished");
                progress.dismiss();
                super.onPageFinished(view, url);
            }
        });
        webView.requestFocus(View.FOCUS_DOWN);
        setContentView(webView);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}


