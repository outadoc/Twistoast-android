/*
 * Twistoast - MainActivity
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

package fr.outadev.twistoast;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoTrafficAlert;
import fr.outadev.twistoast.drawer.NavigationDrawerFragmentItem;
import fr.outadev.twistoast.drawer.NavigationDrawerItem;
import fr.outadev.twistoast.drawer.NavigationDrawerSecondaryItem;
import fr.outadev.twistoast.drawer.NavigationDrawerSeparator;
import fr.outadev.twistoast.drawer.NavigationDrawerWebItem;
import fr.outadev.twistoast.drawer.WebViewFragment;

/**
 * The main activity of the app.
 *
 * @author outadoc
 */
public class MainActivity extends ThemedActivity implements StopsListContainer {

	private List<NavigationDrawerItem> drawerItems;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerList;

	private CharSequence drawerTitle;
	private CharSequence actionBarTitle;

	private int currentFragmentIndex;
	private Fragment[] frags;

	private TimeoTrafficAlert trafficAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		drawerItems = getDrawerItems();
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerTitle = getTitle();

		frags = new Fragment[drawerItems.size()];

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.action_ok,
				R.string.action_delete) {

			@Override
			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(drawerTitle);
				super.onDrawerOpened(drawerView);
			}

			@Override
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(actionBarTitle);
				super.onDrawerClosed(view);
			}
		};

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerLayout.setDrawerListener(drawerToggle);

		drawerList.setAdapter(new NavDrawerArrayAdapter(this, this, R.layout.drawer_list_item, drawerItems,
				currentFragmentIndex));
		drawerList.setItemChecked(currentFragmentIndex, true);

		refreshActionBarTitle();

		if(savedInstanceState != null) {
			currentFragmentIndex = savedInstanceState.getInt("key_current_frag");
			trafficAlert = (TimeoTrafficAlert) savedInstanceState.get("key_traffic_alert");
			checkDrawerItem(currentFragmentIndex);
			displayGlobalTrafficInfo();
		} else {
			currentFragmentIndex = 0;
			loadFragmentFromDrawerPosition(currentFragmentIndex);
			checkForGlobalTrafficInfo();
		}

		TrafficAlertAlarmReceiver.enable(getApplicationContext());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		if(frags[currentFragmentIndex] instanceof WebViewFragment
				&& ((WebViewFragment) frags[currentFragmentIndex]).canGoBack()) {
			((WebViewFragment) frags[currentFragmentIndex]).goBack();
		} else if(currentFragmentIndex == 0) {
			super.onBackPressed();
		} else if(!drawerLayout.isDrawerOpen(Gravity.START)) {
			drawerLayout.openDrawer(Gravity.START);
		} else {
			loadFragmentFromDrawerPosition(0);
		}
	}

	@Override
	public void endRefresh(boolean success) {
		// useless here, wat
	}

	@Override
	public void loadFragmentFromDrawerPosition(int position) {
		currentFragmentIndex = position;

		if(frags[currentFragmentIndex] == null && drawerItems != null && drawerItems.size() > currentFragmentIndex) {
			try {
				frags[currentFragmentIndex] = drawerItems.get(currentFragmentIndex).getFragment();
			} catch(IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, frags[currentFragmentIndex]).commit();

		// Highlight the selected item, update the title, and close the drawer
		checkDrawerItem(currentFragmentIndex);
		drawerLayout.closeDrawer(drawerList);
	}

	public void checkDrawerItem(int position) {
		refreshActionBarTitle();
		drawerList.setItemChecked(position, true);
		((NavDrawerArrayAdapter) drawerList.getAdapter()).setSelectedItemIndex(position);
	}

	public void refreshActionBarTitle() {
		actionBarTitle = getString(drawerItems.get(currentFragmentIndex).getTitleResId());
		getSupportActionBar().setTitle(actionBarTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("key_current_frag", currentFragmentIndex);
		outState.putSerializable("key_traffic_alert", trafficAlert);
	}

	@Override
	protected void onStart() {
		super.onStart();
		displayGlobalTrafficInfo();
	}

	/**
	 * Fetches and stores a global traffic info if there is one available.
	 */
	private void checkForGlobalTrafficInfo() {
		(new AsyncTask<Void, Void, TimeoTrafficAlert>() {

			@Override
			protected TimeoTrafficAlert doInBackground(Void... voids) {
				try {
					return TimeoRequestHandler.getGlobalTrafficAlert(getString(R.string.url_pre_home_info));
				} catch(Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(TimeoTrafficAlert alert) {
				trafficAlert = alert;
				displayGlobalTrafficInfo();
			}

		}).execute();
	}

	/**
	 * Displays a global traffic info if one was downloaded by checkForGlobalTrafficInfo.
	 */
	private void displayGlobalTrafficInfo() {
		View trafficView = findViewById(R.id.view_global_traffic_alert);
		TextView trafficLabel = (TextView) findViewById(R.id.lbl_traffic_info_string);

		if(trafficAlert != null && trafficView != null && trafficLabel != null) {
			Log.i(Utils.TAG, trafficAlert.toString());
			final String url = trafficAlert.getUrl();

			trafficView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					startActivity(intent);
				}

			});

			trafficLabel.setText(trafficAlert.getLabel().replace("Info Trafic", "").trim());
			trafficView.setVisibility(View.VISIBLE);
		} else if(trafficView != null) {
			trafficView.setVisibility(View.GONE);
		}
	}

	/**
	 * Get the navigation drawer items that must be added to the drawer.
	 *
	 * @return a list of NavigationDrawerItems
	 */
	private List<NavigationDrawerItem> getDrawerItems() {
		List<NavigationDrawerItem> list = new ArrayList<>();

		list.add(new NavigationDrawerFragmentItem(R.drawable.ic_schedule, R.string.drawer_item_realtime,
				StopsListFragment.class));
		list.add(new NavigationDrawerWebItem(R.drawable.ic_directions_bus, R.string.drawer_item_timetables,
				getString(R.string.url_drawer_directions)));
		list.add(new NavigationDrawerWebItem(R.drawable.ic_navigation, R.string.drawer_item_routes,
				getString(R.string.url_drawer_navigation)));
		list.add(new NavigationDrawerWebItem(R.drawable.ic_map, R.string.drawer_item_map, getString(R.string.url_drawer_map)));
		list.add(new NavigationDrawerSeparator());
		list.add(new NavigationDrawerWebItem(R.drawable.traffic_cone, R.string.drawer_item_traffic,
				getString(R.string.url_drawer_traffic)));
		list.add(new NavigationDrawerWebItem(R.drawable.ic_books, R.string.drawer_item_news,
				getString(R.string.url_drawer_news)));
		list.add(new NavigationDrawerWebItem(R.drawable.ic_payment, R.string.drawer_item_pricing,
				getString(R.string.url_drawer_pricing)));
		list.add(new NavigationDrawerSeparator());
		list.add(new NavigationDrawerSecondaryItem(R.string.drawer_item_preferences, PrefsFragment.class));

		return list;
	}

}
