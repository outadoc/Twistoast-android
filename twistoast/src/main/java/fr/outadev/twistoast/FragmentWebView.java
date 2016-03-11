/*
 * Twistoast - FragmentWebView
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

package fr.outadev.twistoast;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

/**
 * A fragment that contains a webview and its controls.
 * Will automatically inject some JS in the pages to make their title disappear, for cosmetic reasons.
 */
public class FragmentWebView extends Fragment {

    private WebView mWebView;
    private MenuItem mItemRefresh;
    private MenuItem mItemCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mWebView = new TwistoastWebView(getActivity());

        if (savedInstanceState != null) {
            mWebView.loadUrl(savedInstanceState.getString("url"));
        } else {
            mWebView.loadUrl(getArguments().getString("url"));
        }

        return mWebView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("url", mWebView.getUrl());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.webview, menu);
        mItemRefresh = menu.findItem(R.id.action_refresh_page);
        mItemCancel = menu.findItem(R.id.action_cancel_refresh);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh_page:
                mWebView.reload();
                return true;
            case R.id.action_cancel_refresh:
                mWebView.stopLoading();
                return true;
            case R.id.action_open_website:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(mWebView.getUrl()));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public void goBack() {
        mWebView.goBack();
    }

    /**
     * A custom webview with event listeners and custom settings.
     */
    private class TwistoastWebView extends WebView {

        @SuppressLint("SetJavaScriptEnabled")
        public TwistoastWebView(Context context) {
            super(context);

            setWebViewClient(new WebViewClient() {

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);

                    //hide refresh button
                    if (mItemRefresh != null && mItemCancel != null) {
                        mItemRefresh.setVisible(false);
                        mItemCancel.setVisible(true);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    //hide cancel button
                    if (mItemRefresh != null && mItemCancel != null) {
                        mItemRefresh.setVisible(true);
                        mItemCancel.setVisible(false);
                    }

                    //load some javascript that will hide the stuff we don't want on the page
                    view.loadUrl("javascript: var a = document.getElementsByClassName(\"title-div\");");
                    view.loadUrl("javascript: var b = document.getElementsByClassName(\"contenu\");");
                    view.loadUrl("javascript:" + Uri.encode("for(var i = a.length-1; i >= 0; i--) { (b[0] != null) ? b[0]" +
                            ".removeChild(a[i]) : document.body.removeChild(a[i]); }"));
                }

            });

            //settings
            getSettings().setBuiltInZoomControls(true);
            getSettings().setDisplayZoomControls(false);
            getSettings().setJavaScriptEnabled(true);
        }

        @Override
        public void loadUrl(String url) {
            Map<String, String> map = new HashMap<>();
            map.put("X-Requested-With", "com.actigraph.twisto.tabbarapp");
            super.loadUrl(url, map);
        }

    }

}
