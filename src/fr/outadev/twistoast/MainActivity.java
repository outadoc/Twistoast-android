package fr.outadev.twistoast;

import fr.outadev.twistoast.ui.NavDrawerArrayAdapter;
import fr.outadev.twistoast.ui.PrefsFragment;
import fr.outadev.twistoast.ui.StopsListFragment;
import fr.outadev.twistoast.ui.WebViewFragment;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

			// Called when a drawer has settled in a completely closed state.
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				super.onDrawerClosed(view);
			}

			// Called when a drawer has settled in a completely open state.
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
			}
		};

		// set a custom shadow that overlays the main content when the drawer
		// opens
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);

		// Set the adapter for the list view
		drawerList.setAdapter(new NavDrawerArrayAdapter(this, R.layout.drawer_list_item, drawerEntries));
		drawerList.setItemChecked(0, true);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		if(savedInstanceState == null) {
			selectItem(0, false);
		}
	}

	// Swaps fragments in the main content view
	public void selectItem(int position) {
		selectItem(position, true);
	}

	private void selectItem(int position, boolean addToBackStack) {
		Fragment fragment = null;
		String tag = String.valueOf(position);

		if(position == 0) {
			fragment = new StopsListFragment();
		} else if(position == 4) {
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
			}

			Bundle args = new Bundle();
			args.putString("url", url);
			fragment.setArguments(args);
		}

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();

		if(addToBackStack) {
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(tag).commit();
		} else {
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		}

		// Highlight the selected item, update the title, and close the
		// drawer
		checkDrawerItem(position);
		drawerLayout.closeDrawer(drawerList);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		int pos = 0;
		FragmentManager fm = getFragmentManager();

		if(fm.getBackStackEntryCount() - 1 >= 0) {
			pos = fm.getBackStackEntryCount() - 1;

			if(fm.getBackStackEntryAt(pos).getName() != null) {
				checkDrawerItem(Integer.valueOf(fm.getBackStackEntryAt(pos).getName()));
			} else {
				checkDrawerItem(0);
			}
		} else {
			checkDrawerItem(0);
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
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if(drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	private String[] drawerEntries;
	private DrawerLayout drawerLayout;
	private ListView drawerList;

	protected CharSequence mDrawerTitle;
	private CharSequence mTitle;

	ActionBarDrawerToggle drawerToggle;

}
