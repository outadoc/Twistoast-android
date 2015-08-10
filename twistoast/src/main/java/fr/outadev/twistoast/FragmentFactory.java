package fr.outadev.twistoast;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

/**
 * A factory that instantiates fragments for the navigation drawer, using the menu item ID.
 */
public class FragmentFactory {

	/**
	 * Gets a new Fragment corresponding to the specified menu item ID.
	 * @param context A context
	 * @param itemId The menu item's identifier
	 * @return A new fragment, or null if no corresponding fragment could be found.
	 */
	public static Fragment getFragmentFromMenuItem(Context context, int itemId) {
		switch(itemId) {
			case R.id.drawer_realtime:
				return new StopsListFragment();
			case R.id.drawer_timetables:
				return getWebViewFragment(context.getString(R.string.url_drawer_timetables));
			case R.id.drawer_routes:
				return getWebViewFragment(context.getString(R.string.url_drawer_navigation));
			case R.id.drawer_map:
				return getWebViewFragment(context.getString(R.string.url_drawer_map));
			case R.id.drawer_traffic:
				return getWebViewFragment(context.getString(R.string.url_drawer_traffic));
			case R.id.drawer_news:
				return getWebViewFragment(context.getString(R.string.url_drawer_news));
			case R.id.drawer_pricing:
				return getWebViewFragment(context.getString(R.string.url_drawer_pricing));
			case R.id.drawer_settings:
				return new PreferencesFragment();
			case R.id.drawer_about:
				return new AboutFragment();
		}

		return null;
	}

	private static Fragment getWebViewFragment(String url) {
		Fragment frag = new WebViewFragment();
		Bundle args = new Bundle();
		args.putString("url", url);
		frag.setArguments(args);
		return frag;
	}

}
