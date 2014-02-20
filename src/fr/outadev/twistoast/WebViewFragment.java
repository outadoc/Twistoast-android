package fr.outadev.twistoast;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends Fragment {

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		WebView webview = new WebView(getActivity());
		
		webview.setWebViewClient(new WebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		
		webview.loadUrl(getArguments().getString("url"));
		
		return webview;
	}

}
