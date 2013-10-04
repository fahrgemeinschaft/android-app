/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft.util;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import de.fahrgemeinschaft.inappbilling.util.IabHelper;
import de.fahrgemeinschaft.inappbilling.util.IabResult;
import de.fahrgemeinschaft.inappbilling.util.Inventory;
import de.fahrgemeinschaft.inappbilling.util.Purchase;

import de.fahrgemeinschaft.R;
import de.fahrgemeinschaft.Secret;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
public class WebActivity extends SherlockActivity {

    private static final String TAG = "Fahrgemeinschaft";
    private ProgressDialog progress;
    private WebView webView;

    static final String ITEM_SKU = "android.test.refunded";
    IabHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        progress = new ProgressDialog(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
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
        
        String base64EncodedPublicKey = Secret.LICENSE_KEY;
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");
                
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
//                        complain("Problem setting up in-app billing: " + result);
                    return;
                }
                
                // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
        webView.loadUrl(getIntent().getDataString());
        webView.requestFocus(View.FOCUS_DOWN);
        setContentView(webView);
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                // ERROR
                return;
            }
            Log.d(TAG, "Query inventory was successful.");
            //buyClick(this);
        }
    };

    public void buyClick(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,   
              mPurchaseFinishedListener, "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                Intent data) {
          if (!mHelper.handleActivityResult(requestCode, 
                  resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
          }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener 
                        = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, 
                    Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }
        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener 
                   = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                   Inventory inventory) {
            if (result.isFailure()) {
            // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU), 
                mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
                new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, 
                            IabResult result) {
            if (result.isSuccess()) {
                // SUCCESS! show thank you message
            } else {
                // handle error
            }
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        overridePendingTransition(
                R.anim.do_nix, R.anim.slide_out_bottom);
        return super.onKeyUp(keyCode, event);
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
