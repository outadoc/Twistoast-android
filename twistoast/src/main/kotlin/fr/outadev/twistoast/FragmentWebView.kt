/*
 * Twistoast - FragmentWebView.kt
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

package fr.outadev.twistoast

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * A fragment that contains a webview and its controls.
 * Will automatically inject some JS in the pages to make their title disappear, for cosmetic reasons.
 */
class FragmentWebView : Fragment() {

    private var webView: WebView? = null

    private var itemRefresh: MenuItem? = null
    private var itemCancel: MenuItem? = null

    private var urlToOpen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (webView == null) {
            webView = TwistoastWebView(activity)

            if (arguments.containsKey("url")) {
                loadUrl(arguments.getString("url"))
            } else if (arguments.containsKey("twitter_username")) {
                loadTwitterTimeline(arguments.getString("twitter_username"))
            }
        }

        return webView
    }

    private fun loadTwitterTimeline(username: String) {
        urlToOpen = "https://twitter.com/$username"
        webView?.loadData("<html><body><a class=\"twitter-timeline\" href=\"https://twitter.com/$username\">" +
                "Tweets by TwistoCaen</a> " +
                "<script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script></body></html>",
                "text/html", "utf-8")
    }

    private fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.webview, menu)
        itemRefresh = menu.findItem(R.id.action_refresh_page)
        itemCancel = menu.findItem(R.id.action_cancel_refresh)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        when (item.itemId) {
            R.id.action_refresh_page -> {
                webView?.reload()
                return true
            }
            R.id.action_cancel_refresh -> {
                webView?.stopLoading()
                return true
            }
            R.id.action_open_website -> {
                if (urlToOpen == null)
                    return true

                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(urlToOpen)
                startActivity(i)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    val canGoBack: Boolean
        get() = webView!!.canGoBack()

    fun goBack() {
        webView?.goBack()
    }

    /**
     * A custom webview with event listeners and custom settings.
     */
    private inner class TwistoastWebView
    constructor(context: Context) : WebView(context) {

        val headers = mapOf(Pair("X-Requested-With", "com.actigraph.twisto.tabbarapp"))

        init {
            setWebViewClient(object : WebViewClient() {

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                    //hide refresh button
                    itemRefresh?.isVisible = false
                    itemCancel?.isVisible = true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)

                    //hide cancel button
                    itemRefresh?.isVisible = true
                    itemCancel?.isVisible = false

                    //load some javascript that will hide the stuff we don't want on the page
                    view.loadUrl("javascript: var a = document.getElementsByClassName(\"title-div\");")
                    view.loadUrl("javascript: var b = document.getElementsByClassName(\"contenu\");")
                    view.loadUrl("javascript:" + Uri.encode("for(var i = a.length-1; i >= 0; i--) { " +
                            "(b[0] != null) ? b[0].removeChild(a[i]) : document.body.removeChild(a[i]); }"))
                }

            })

            //settings
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.javaScriptEnabled = true
        }

        override fun loadUrl(url: String) {
            super.loadUrl(url, headers)

            if (Regex("^https?:").find(url) != null) {
                urlToOpen = url
            }
        }

    }

}
