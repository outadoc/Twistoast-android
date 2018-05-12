/*
 * Twistoast - FragmentWebView.kt
 * Copyright (C) 2013-2018 Baptiste Candellier
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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
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
            webView = TwistoastWebView(context!!)

            arguments?.let {
                if (it.containsKey("url")) {
                    // Load a classic URL
                    loadUrl(it.getString("url"))
                } else if (it.containsKey("twitter_username")) {
                    // Load a Twitter profile
                    loadTwitterTimeline(it.getString("twitter_username"))
                }
            }
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }*/

        return webView
    }

    private fun loadTwitterTimeline(username: String) {
        // Load a twitter profile page by injecting the username into some HTML that will be loaded
        // by the webview
        urlToOpen = "https://twitter.com/$username"

        val twitterHtmlWrapper = """<html><body><a class="twitter-timeline" href="https://twitter.com/$username">""" +
                """Tweets by TwistoCaen</a> """ +
                """<script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script></body></html>"""

        webView?.loadData(twitterHtmlWrapper, "text/html", "utf-8")
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
        return when (item.itemId) {
            R.id.action_refresh_page -> {
                webView?.reload()
                true
            }

            R.id.action_cancel_refresh -> {
                webView?.stopLoading()
                true
            }

            R.id.action_open_website -> {
                if (urlToOpen == null)
                    return true

                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(urlToOpen)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    val canGoBack: Boolean
        get() = webView?.canGoBack() ?: false

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
            webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                    // Hide refresh button while refreshing
                    itemRefresh?.isVisible = false
                    itemCancel?.isVisible = true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)

                    // Hide cancel button when done refreshing
                    itemRefresh?.isVisible = true
                    itemCancel?.isVisible = false

                    // Load some javascript that will hide the stuff we don't want on the page
                    view.loadUrl("""javascript: var elements = [document.getElementById("tx_cookies"), document.getElementById("haut"), document.getElementById("menu-sec"), document.getElementsByClassName("titre")[0]];""")
                    view.loadUrl("""javascript: var b = document.getElementsByClassName("contenu");""")
                    view.loadUrl("javascript:" + Uri.encode("for (var i = 0; i < elements.length; i++) { if (elements[i] === null) continue; elements[i].parentNode.removeChild(elements[i]); }"))
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.isHttpScheme) {
                        urlToOpen = url
                    }

                    // Intercept page load so that we can inject our own X-Requested-With header
                    // because the Twisto website checks to see if it's coming from their own app
                    // (and as you can see it's useless as hell)
                    view.loadUrl(url, headers)
                    return true
                }

            }

            with(settings) {
                builtInZoomControls = true
                displayZoomControls = false
                javaScriptEnabled = true
            }
        }

        override fun loadUrl(url: String) {
            super.loadUrl(url, headers)

            // If this is a http or https URL, remember it
            // so that we can open it in a browser later on if needed
            if (url.isHttpScheme) {
                urlToOpen = url
            }
        }

        val String.isHttpScheme: Boolean
            get() = Regex("^https?:").find(this) != null

    }
}
