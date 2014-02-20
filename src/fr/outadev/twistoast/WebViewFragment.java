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
		
		String url = getArguments().getString("url");
		
		if(url == SCHEDULES_URL)
			webview.setInitialScale(180);
	    
		webview.loadUrl(url);
		return webview;
	}
	
	public static final String SCHEDULES_URL = "http://caen.prod.navitia.com/Navitia/HP_2_Line.asp?NetworkList=1|CAE8|twisto";
	public static final String ROUTES_URL = "http://twisto.mobi/774-Itin%C3%A9raire.html";
	public static final String TRAFFIC_INFO_URL = "http://twisto.mobi/777-Info%20trafic.html";

}
