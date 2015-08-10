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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoTrafficAlert;
import fr.outadev.twistoast.background.NextStopAlarmReceiver;
import fr.outadev.twistoast.background.TrafficAlertAlarmReceiver;

/**
 * The main activity of the app.
 *
 * @author outadoc
 */
public class MainActivity extends ThemedActivity implements IStopsListContainer, NavigationView.OnNavigationItemSelectedListener {

	public static final int DEFAULT_DRAWER_ITEM = R.id.drawer_realtime;

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private Menu drawerMenu;
	private NavigationView navigationView;

	private CharSequence drawerTitle;
	private CharSequence actionBarTitle;

	private int currentDrawerItem;
	private HashMap<Integer, Fragment> frags;

	private TimeoTrafficAlert trafficAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		// Drawer config
		frags = new HashMap<>();
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerTitle = getTitle();

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
		navigationView = (NavigationView) findViewById(R.id.drawer_nav_view);
		drawerMenu = navigationView.getMenu();

		navigationView.setNavigationItemSelectedListener(this);
		refreshActionBarTitle();

		// Handle saved variables and check traffic info if needed
		if(savedInstanceState != null) {
			currentDrawerItem = savedInstanceState.getInt("key_current_drawer_item");
			trafficAlert = (TimeoTrafficAlert) savedInstanceState.get("key_traffic_alert");
			displayGlobalTrafficInfo();
		} else {
			currentDrawerItem = DEFAULT_DRAWER_ITEM;
			loadFragmentForDrawerItem(currentDrawerItem);
			checkForGlobalTrafficInfo();
		}

		// Turn the notifications back off if necessary
		Database db = new Database(DatabaseOpenHelper.getInstance(this));
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(db.getWatchedStopsCount() > 0) {
			NextStopAlarmReceiver.enable(getApplicationContext());
		}

		if(prefs.getBoolean("pref_enable_notif_traffic", true)) {
			TrafficAlertAlarmReceiver.enable(getApplicationContext());
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	protected void onStart() {
		super.onStart();
		displayGlobalTrafficInfo();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		Fragment frag = frags.get(currentDrawerItem);

		if(frag instanceof WebViewFragment
				&& ((WebViewFragment) frag).canGoBack()) {
			// If we can move back of a page in the browser, do it
			((WebViewFragment) frag).goBack();
		} else if(currentDrawerItem == DEFAULT_DRAWER_ITEM) {
			// If we're on the main screen, just exit
			super.onBackPressed();
		} else if(!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
			// If the drawer isn't opened, open it
			drawerLayout.openDrawer(Gravity.LEFT);
		} else {
			// Otherwise, go back to the main screen
			loadFragmentForDrawerItem(DEFAULT_DRAWER_ITEM);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("key_current_drawer_item", currentDrawerItem);
		outState.putSerializable("key_traffic_alert", trafficAlert);
	}

	@Override
	public void endRefresh(boolean success) {
		// useless here, wat
	}

	@Override
	public void loadFragmentForDrawerItem(int itemId) {
		currentDrawerItem = itemId;
		Fragment fragmentToOpen;

		if(frags.containsKey(itemId)) {
			fragmentToOpen = frags.get(itemId);
		} else {
			fragmentToOpen = FragmentFactory.getFragmentFromMenuItem(this, itemId);
		}

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentToOpen).commit();

		// Highlight the selected item, update the title, and close the drawer
		drawerLayout.closeDrawer(Gravity.LEFT);
	}


	public void refreshActionBarTitle() {
		//actionBarTitle = drawerMenu.findItem(currentDrawerItem).getTitle();
		getSupportActionBar().setTitle(actionBarTitle);
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

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		loadFragmentForDrawerItem(menuItem.getItemId());
		return true;
	}
}
