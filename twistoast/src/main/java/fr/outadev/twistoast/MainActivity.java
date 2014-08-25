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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import fr.outadev.android.timeo.KeolisRequestHandler;
import fr.outadev.android.timeo.model.TimeoTrafficAlert;
import fr.outadev.twistoast.ui.NavDrawerArrayAdapter;
import fr.outadev.twistoast.ui.PrefsFragment;
import fr.outadev.twistoast.ui.StopsListFragment;
import fr.outadev.twistoast.ui.WebViewFragment;

public class MainActivity extends Activity {

	private String[] drawerEntries;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerList;

	private CharSequence drawerTitle;
	private CharSequence actionBarTitle;

	private int currentFragmentIndex;
	private Fragment frags[];

	private TimeoTrafficAlert trafficAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		drawerEntries = getResources().getStringArray(R.array.drawer_entries);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerTitle = getTitle();

		frags = new Fragment[drawerEntries.length];

		if(savedInstanceState != null) {
			currentFragmentIndex = savedInstanceState.getInt("key_current_frag");
		} else {
			currentFragmentIndex = 0;
		}

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_navigation_drawer, R.string.action_ok,
				R.string.action_delete) {

			@Override
			public void onDrawerClosed(View view) {
				if(getActionBar() != null) {
					getActionBar().setTitle(actionBarTitle);
				}
				super.onDrawerClosed(view);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				if(getActionBar() != null) {
					getActionBar().setTitle(drawerTitle);
				}
				super.onDrawerOpened(drawerView);
			}
		};

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerLayout.setDrawerListener(drawerToggle);

		if(getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}

		actionBarTitle = drawerEntries[currentFragmentIndex];
		setTitle(actionBarTitle);

		drawerList.setAdapter(new NavDrawerArrayAdapter(this, R.layout.drawer_list_item, drawerEntries, currentFragmentIndex));
		drawerList.setItemChecked(currentFragmentIndex, true);

		if(savedInstanceState == null) {
			loadFragmentFromDrawerPosition(currentFragmentIndex);
		}

		checkForGlobalTrafficInfo();
	}

	@Override
	protected void onStart() {
		super.onStart();
		displayGlobalTrafficInfo();
	}

	// Swaps fragments in the main content view
	public void loadFragmentFromDrawerPosition(int position) {
		currentFragmentIndex = position;

		if(frags[currentFragmentIndex] == null) {
			switch(position) {
				case 0:
					frags[currentFragmentIndex] = new StopsListFragment();
					break;
				case 7:
					frags[currentFragmentIndex] = new PrefsFragment();
					break;
				default: {
					String url = "";
					frags[currentFragmentIndex] = new WebViewFragment();

					switch(position) {
						case 1:
							url = getResources().getString(R.string.timetables_url);
							break;
						case 2:
							url = getResources().getString(R.string.routes_url);
							break;
						case 3:
							url = getResources().getString(R.string.map_url);
							break;
						case 4:
							url = getResources().getString(R.string.traffic_url);
							break;
						case 5:
							url = getResources().getString(R.string.news_url);
							break;
						case 6:
							url = getResources().getString(R.string.prices_url);
							break;
					}

					Bundle args = new Bundle();
					args.putString("url", url);
					frags[currentFragmentIndex].setArguments(args);

					break;
				}
			}
		}

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, frags[currentFragmentIndex]).commit();

		// Highlight the selected item, update the title, and close the drawer
		checkDrawerItem(currentFragmentIndex);
		drawerLayout.closeDrawer(drawerList);
	}

	@Override
	public void onBackPressed() {
		if(frags[currentFragmentIndex] instanceof WebViewFragment && ((WebViewFragment) frags[currentFragmentIndex]).canGoBack
				()) {
			((WebViewFragment) frags[currentFragmentIndex]).goBack();
		} else if(currentFragmentIndex == 0) {
			super.onBackPressed();
		} else {
			drawerLayout.openDrawer(Gravity.START);
		}
	}

	public void checkDrawerItem(int position) {
		actionBarTitle = drawerEntries[position];
		if(getActionBar() != null) {
			getActionBar().setTitle(actionBarTitle);
		}

		drawerList.setItemChecked(position, true);
		((NavDrawerArrayAdapter) drawerList.getAdapter()).setSelectedItemIndex(position);
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

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("key_current_frag", currentFragmentIndex);
	}

	private void checkForGlobalTrafficInfo() {
		(new AsyncTask<Void, Void, TimeoTrafficAlert>() {

			@Override
			protected TimeoTrafficAlert doInBackground(Void... voids) {
				return (new KeolisRequestHandler()).getGlobalTrafficAlert();
			}

			@Override
			protected void onPostExecute(TimeoTrafficAlert alert) {
				trafficAlert = alert;
				displayGlobalTrafficInfo();
			}

		}).execute();
	}

	private void displayGlobalTrafficInfo() {
		View trafficView = findViewById(R.id.view_global_traffic_alert);
		TextView trafficLabel = (TextView) findViewById(R.id.lbl_traffic_info_string);

		if(trafficAlert != null && trafficView != null && trafficLabel != null) {
			Log.i("SkinSwitch", trafficAlert.toString());
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

}
