package de.fahrgemeinschaft.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.JsonObjectRequest;

import de.fahrgemeinschaft.FahrgemeinschaftConnector;
import de.fahrgemeinschaft.Secret;

public class ProfileRequest extends JsonObjectRequest {

    private static final String E_TAG = "ETag";
    private static final String DATE = "Date";
    private HashMap<String, String> headers;
    static final String PROFILE_URL =
            FahrgemeinschaftConnector.FAHRGEMEINSCHAFT_DE + "/user/";

    public ProfileRequest(String userid, Listener<JSONObject> listener,
            ErrorListener errorListener) {
        super(Method.GET, PROFILE_URL + userid,
                null, listener, errorListener);
        setShouldCache(Boolean.TRUE);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse res) {
        return Response.success(super.parseNetworkResponse(res).result,
                parseIgnoreCacheHeaders(res));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers();
    };

    public Map<String, String> headers() {
        if (headers == null) {
            headers = new HashMap<String, String>();
            headers.put(FahrgemeinschaftConnector.APIKEY, Secret.APIKEY);
        }
        return headers;
    }

    public static final ImageCache imageCache = new ImageCache() {
        @Override
        public void putBitmap(String key, Bitmap value) {
            mImageCache.put(key, value);
        }

        @Override
        public Bitmap getBitmap(String key) {
            return mImageCache.get(key);
        }
    };

    private static final LruCache<String, Bitmap> mImageCache
            = new LruCache<String, Bitmap>(20);

    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
        long now = System.currentTimeMillis();
        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverEtag = null;
        String headerValue;
        headerValue = headers.get(DATE);
        if (headerValue != null) {
            // serverDate = parseDateAsEpoch(headerValue);
        }
        serverEtag = headers.get(E_TAG);
        final long cacheHitButRefreshed = 3 * 60 * 1000;
        // in 3 minutes cache will be hit, but also refreshed on background
        final long cacheExpired = 24 * 60 * 60 * 1000;
        // in 24 hours this cache entry expires completely
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;
        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;
        return entry;
    }
}