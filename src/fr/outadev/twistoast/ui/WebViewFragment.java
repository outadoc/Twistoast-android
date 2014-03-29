package fr.outadev.twistoast.ui;

import fr.outadev.twistoast.R;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends Fragment {

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

			setWebViewClient(new WebViewClient());

			getSettings().setBuiltInZoomControls(true);
			getSettings().setDisplayZoomControls(false);
			getSettings().setJavaScriptEnabled(true);
		}

	}

	private WebView webView;

}
