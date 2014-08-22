/*
 * Twistoast - WebViewFragment
 * Copyright (C) 2013-2014  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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

import fr.outadev.twistoast.R;

public class WebViewFragment extends Fragment {

	private WebView webView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		webView = new TwistoastWebView(getActivity());
		webView.loadUrl(getArguments().getString("url"));

		return webView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.webview, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch(item.getItemId()) {
			case R.id.action_refresh_page:
				webView.reload();
				return true;
			case R.id.action_open_website:
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(webView.getUrl()));
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public boolean canGoBack() {
		return webView.canGoBack();
	}

	public void goBack() {
		webView.goBack();
	}

	private class TwistoastWebView extends WebView {

		@SuppressLint("SetJavaScriptEnabled")
		public TwistoastWebView(Context context) {
			super(context);

			setWebViewClient(new WebViewClient() {

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					view.loadUrl("javascript: var a = document.getElementsByClassName(\"title-div\");");
					view.loadUrl("javascript: var b = document.getElementsByClassName(\"contenu\");");
					view.loadUrl("javascript:" + Uri.encode("for(var i = a.length-1; i >= 0; i--) { (b[0] != null) ? b[0]" +
							".removeChild(a[i]) : " +
							"document.body.removeChild(a[i]); }"));
				}

			});

			getSettings().setBuiltInZoomControls(true);
			getSettings().setDisplayZoomControls(false);
			getSettings().setJavaScriptEnabled(true);
		}

	}

}
