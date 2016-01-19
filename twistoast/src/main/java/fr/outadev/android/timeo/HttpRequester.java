/*
 * Twistoast - HttpRequester
 * Copyright (C) 2013-2016 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.android.timeo;

import android.util.Log;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Baptiste on 2016-01-18.
 */
public class HttpRequester {

    public final static String TAG = HttpRequester.class.getName();
    private static HttpRequester sInstance;

    private HttpRequester() {
    }

    public static HttpRequester getInstance() {
        if (sInstance == null) {
            sInstance = new HttpRequester();
        }

        return sInstance;
    }

    /**
     * Requests a web page via an HTTP GET request.
     *
     * @param url       URL to fetch
     * @param params    HTTP GET parameters as a string (e.g. foo=bar&bar=foobar)
     * @param useCaches true if the client can cache the request
     * @return the raw body of the page
     * @throws IOException if an HTTP error occurred
     */
    public String requestWebPage(String url, String params, boolean useCaches) throws IOException {
        String finalUrl = url + "?" + params;
        Log.i(TAG, "requesting " + finalUrl);

        OkHttpClient client = new OkHttpClient();

        Request.Builder builder = new Request.Builder()
                .url(finalUrl);

        if (!useCaches) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }

        Response response = client.newCall(builder.build()).execute();
        return response.body().string();
    }

    /**
     * Requests a web page via an HTTP GET request.
     *
     * @param url       URL to fetch
     * @param useCaches true if the client can cache the request
     * @return the raw body of the page
     * @throws IOException if an HTTP error occurred
     */
    public String requestWebPage(String url, boolean useCaches) throws IOException {
        return requestWebPage(url, "", useCaches);
    }

}
