/*
 * Twistoast - HttpRequester.kt
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

package fr.outadev.android.transport

import android.util.Log
import com.squareup.okhttp.CacheControl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.io.IOException

/**
 * An implementation for the IHttpRequester.
 *
 * @author outadoc
 */
class HttpRequester : IHttpRequester {

    private val httpClient = OkHttpClient()

    /**
     * Requests a web page via an HTTP GET request.

     * @param url       URL to fetch
     * @param useCaches true if the client can cache the request
     * @return the raw body of the page
     *
     * @throws IOException if an HTTP error occurred
     */
    @Throws(IOException::class)
    override fun requestWebPage(url: String, params: String, useCaches: Boolean): String {
        val finalUrl = if (params.isEmpty()) url else url + "?" + params
        Log.i(TAG, "requesting " + finalUrl)

        val builder = Request.Builder().url(finalUrl)

        if (!useCaches) {
            builder.cacheControl(CacheControl.FORCE_NETWORK)
        }

        val response = httpClient.newCall(builder.build()).execute()
        return response.body().string()
    }

    companion object {

        val TAG = HttpRequester::class.java.simpleName!!
    }

}
