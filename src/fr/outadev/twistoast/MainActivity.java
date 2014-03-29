package fr.outadev.twistoast;

import fr.outadev.twistoast.ui.NavDrawerArrayAdapter;
import fr.outadev.twistoast.ui.PrefsFragment;
import fr.outadev.twistoast.ui.StopsListFragment;
import fr.outadev.twistoast.ui.WebViewFragment;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		drawerEntries = getResources().getStringArray(R.array.drawer_entries);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);

		drawerTitle = getTitle();
		actionBarTitle = drawerEntries[0];
		setTitle(actionBarTitle);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.action_ok,
		        R.string.action_delete) {

			public void onDrawerClosed(View view) {
				getActionBar().setTitle(actionBarTitle);
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(drawerTitle);
				super.onDrawerOpened(drawerView);
			}
		};

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerLayout.setDrawerListener(drawerToggle);

		drawerList.setAdapter(new NavDrawerArrayAdapter(this, R.layout.drawer_list_item, drawerEntries));
		drawerList.setItemChecked(0, true);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		if(savedInstanceState == null) {
			loadFragmentFromDrawerPosition(0);
		}
	}

	// Swaps fragments in the main content view
	public void loadFragmentFromDrawerPosition(int position) {

		if(position == 0) {
			currentFragment = new StopsListFragment();
		} else if(position == 5) {
			currentFragment = new PrefsFragment();
		} else {
			String url = new String();
			currentFragment = new WebViewFragment();

			switch(position) {
				case 1:
					url = getResources().getString(R.string.timetables_url);
					break;
				case 2:
					url = getResources().getString(R.string.routes_url);
					break;
				case 3:
					url = getResources().getString(R.string.traffic_url);
					break;
				case 4:
					url = getResources().getString(R.string.news_url);
					break;
			}

			Bundle args = new Bundle();
			args.putString("url", url);
			currentFragment.setArguments(args);
		}

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment, MAIN_FRAGMENT_TAG).commit();

		// Highlight the selected item, update the title, and close the
		// drawer
		checkDrawerItem(position);
		drawerLayout.closeDrawer(drawerList);

	}

	@Override
	public void onBackPressed() {
		if(currentFragment instanceof WebViewFragment && ((WebViewFragment) currentFragment).canGoBack()) {
			((WebViewFragment) currentFragment).goBack();
		} else if(currentFragment instanceof StopsListFragment) {
			super.onBackPressed();
		} else {
			drawerLayout.openDrawer(Gravity.LEFT);
		}
	}

	public void checkDrawerItem(int position) {
		actionBarTitle = drawerEntries[position];
		getActionBar().setTitle(actionBarTitle);

		drawerList.setItemChecked(position, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private static final String MAIN_FRAGMENT_TAG = "MAINFRAG";

	private String[] drawerEntries;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerList;

	private CharSequence drawerTitle;
	private CharSequence actionBarTitle;

	private Fragment currentFragment;
}
