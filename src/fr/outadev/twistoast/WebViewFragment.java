package fr.outadev.twistoast;

import android.annotation.SuppressLint;
import android.app.Fragment;
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

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		webview = new WebView(getActivity());

		webview.setWebViewClient(new WebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		
		String url = getArguments().getString("url");
		
		if(url == SCHEDULES_URL)
			webview.setInitialScale(180);
	    
		webview.loadUrl(url);
		return webview;
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
		switch (item.getItemId()) {
			case R.id.action_refresh_page :
				webview.reload();
				return true;
			default :
				return super.onOptionsItemSelected(item);
		}
	}
	
	private WebView webview;
	
	public static final String SCHEDULES_URL = "http://caen.prod.navitia.com/Navitia/HP_2_Line.asp?NetworkList=1|CAE8|twisto";
	public static final String ROUTES_URL = "http://twisto.mobi/774-Itin%C3%A9raire.html";
	public static final String TRAFFIC_INFO_URL = "http://twisto.mobi/777-Info%20trafic.html";

}
