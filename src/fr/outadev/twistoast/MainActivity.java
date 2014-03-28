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

		mDrawerTitle = getTitle();
		mTitle = drawerEntries[0];
		setTitle(mTitle);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.action_ok,
		        R.string.action_delete) {

			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
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
			selectItem(0);
		}
	}

	// Swaps fragments in the main content view
	public void selectItem(int position) {
		Fragment fragment = null;

		if(position == 0) {
			fragment = new StopsListFragment();
		} else if(position == 5) {
			fragment = new PrefsFragment();
		} else {
			String url = new String();
			fragment = new WebViewFragment();

			switch(position) {
				case 1:
					url = WebViewFragment.SCHEDULES_URL;
					break;
				case 2:
					url = WebViewFragment.ROUTES_URL;
					break;
				case 3:
					url = WebViewFragment.TRAFFIC_INFO_URL;
					break;
				case 4:
					url = WebViewFragment.NEWS_INFO_URL;
					break;
			}

			Bundle args = new Bundle();
			args.putString("url", url);
			fragment.setArguments(args);
		}

		currentFragmentIndex = position;

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		// Highlight the selected item, update the title, and close the
		// drawer
		checkDrawerItem(position);
		drawerLayout.closeDrawer(drawerList);

	}

	@Override
	public void onBackPressed() {
		if(currentFragmentIndex > 0) {
			drawerLayout.openDrawer(Gravity.LEFT);
		} else {
			super.onBackPressed();
		}
	}

	public void checkDrawerItem(int position) {
		mTitle = drawerEntries[position];
		getActionBar().setTitle(mTitle);

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

	private String[] drawerEntries;
	private DrawerLayout drawerLayout;
	private ListView drawerList;

	private int currentFragmentIndex;

	protected CharSequence mDrawerTitle;
	private CharSequence mTitle;

	ActionBarDrawerToggle drawerToggle;

}
