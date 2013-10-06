/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft.util;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;

import de.fahrgemeinschaft.R;
import de.fahrgemeinschaft.Secret;
import de.fahrgemeinschaft.inappbilling.util.IabHelper;
import de.fahrgemeinschaft.inappbilling.util.IabHelper.OnConsumeFinishedListener;
import de.fahrgemeinschaft.inappbilling.util.IabHelper.OnIabPurchaseFinishedListener;
import de.fahrgemeinschaft.inappbilling.util.IabHelper.OnIabSetupFinishedListener;
import de.fahrgemeinschaft.inappbilling.util.IabResult;
import de.fahrgemeinschaft.inappbilling.util.Inventory;
import de.fahrgemeinschaft.inappbilling.util.Purchase;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
public class WebActivity extends SherlockActivity 
        implements OnIabPurchaseFinishedListener,
        OnConsumeFinishedListener, OnIabSetupFinishedListener {

    private static final String TAG = "Fahrgemeinschaft";
    private ProgressDialog progress;
    private WebView webView;

    IabHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        progress = new ProgressDialog(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        String base64EncodedPublicKey = Secret.LICENSE_KEY;
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(this);
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
                overridePendingTransition(
                        R.anim.do_nix, R.anim.slide_out_bottom);
                super.onPageFinished(view, url);
            }
        });
        
        webView.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public String donate(String amount) {
                String sku = "de.fahrgemeinschaft.donate_" + amount;
                // TESTING //
                sku = "android.test.purchased";
                // TESTING //
                mHelper.launchPurchaseFlow(WebActivity.this,
                        sku, 0, WebActivity.this, "");
                return null;
            }
            @JavascriptInterface
            public String close() {
                finish();
                return null;
            }
        }, "fg");
    }

    public void onIabSetupFinished(IabResult result) {
        Log.d(TAG, "Setup finished.");
        
        if (!result.isSuccess()) {
            // Oh noes, there was a problem.
            Crouton.makeText(WebActivity.this, "geht ned", Style.ALERT);
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                Intent data) {
          if (!mHelper.handleActivityResult(requestCode, 
                  resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
          }
    }

    public void onIabPurchaseFinished(IabResult result, 
            Purchase purchase) {
        if (result.isFailure()) {
            // Handle error
            return;
        }
        else {
            webView.loadUrl("");
            mHelper.consumeAsync(purchase, this);
        }
    }

    public void onConsumeFinished(Purchase purchase, 
            IabResult result) {
        if (result.isSuccess()) {
            // SUCCESS! show thank you message
        } else {
            // handle error
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.do_nix, R.anim.slide_out_bottom);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
            mHelper = null;
    }

}
