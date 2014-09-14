/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE.. 
 *
 */

package de.fahrgemeinschaft.util;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;

import de.fahrgemeinschaft.R;
import de.fahrgemeinschaft.Secret;
import de.fahrgemeinschaft.inappbilling.util.IabException;
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

    private static final String DONATE_URL = "http://sonnenstreifen.de/kunden/fahrgemeinschaft/spendenstand.php?b=";
    private ProgressDialog progress;
    private WebView webView;

    private Handler handler;
    IabHelper mHelper;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        progress = new ProgressDialog(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        String base64EncodedPublicKey = Secret.LICENSE_KEY;
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(this);
        webView.loadUrl(getIntent().getDataString());
        webView.requestFocus(View.FOCUS_DOWN);
        setContentView(webView);
        handler = new Handler();
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
        
        webView.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public String donate(String amount) {
                String sku = "de.fahrgemeinschaft.donate_" + amount;
                ArrayList<String> skus = new ArrayList<String>();
                skus.add(sku);
                try {
                    Inventory inventory = mHelper.queryInventory(true, skus);
                    Purchase purchase = inventory.getPurchase(sku);
                    if (purchase != null) {
                        consume(purchase);
                    } else {
                        mHelper.launchPurchaseFlow(WebActivity.this,
                                sku, 0, WebActivity.this, "");
                    }
                } catch (IabException e) {
                    e.printStackTrace();
                }
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
        if (!result.isSuccess()) {
            Crouton.makeText(WebActivity.this, "Payment error", Style.ALERT);
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

    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        if (result.isSuccess()) {
            SharedPreferences prefs = this.getSharedPreferences(
                    "de.fahrgemeinschaft", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("free", true).commit();
            consume(purchase);
        }
    }

    private void consume(final Purchase purchase) {
        final String amount = purchase.getSku().split("_")[1];
        handler.post(new Runnable() {
            
            @Override
            public void run() {
                webView.loadUrl(DONATE_URL + amount + Secret.DONATE_KEY);
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
                        super.onPageFinished(view, url);
                        mHelper.consumeAsync(purchase, WebActivity.this);
                    }
                });
            }
        });
    }

    public void onConsumeFinished(Purchase purchase, IabResult result) {}

    @Override
    public void onBackPressed() {
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
