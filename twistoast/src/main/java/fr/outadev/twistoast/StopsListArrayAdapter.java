/*
 * Twistoast - StopsListArrayAdapter
 * Copyright (C) 2013-2015 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.outadev.android.timeo.ScheduleTime;
import fr.outadev.android.timeo.TimeoBlockingMessageException;
import fr.outadev.android.timeo.TimeoException;
import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoSingleSchedule;
import fr.outadev.android.timeo.TimeoStop;
import fr.outadev.android.timeo.TimeoStopSchedule;

/**
 * An array adapter for the main list of bus stops.
 *
 * @author outadoc
 */
public class StopsListArrayAdapter extends ArrayAdapter<TimeoStop> {

	private final IStopsListContainer stopsListContainer;
	private final View parentView;
	private final Activity activity;
	private final Database db;

	private final List<TimeoStop> stops;
	private final Map<TimeoStop, TimeoStopSchedule> schedules;
	private final SparseArray<String> networks;

	private int networkCount = 0;
	private int nbOutdatedStops = 0;

	public StopsListArrayAdapter(Activity activity, int resource, List<TimeoStop> stops,
	                             IStopsListContainer stopsListContainer, View parentView) {
		super(activity, resource, stops);

		this.activity = activity;
		this.stops = stops;
		this.stopsListContainer = stopsListContainer;
		this.parentView = parentView;
		this.schedules = new HashMap<>();
		this.networks = TimeoRequestHandler.getNetworksList();
		this.db = new Database(DatabaseOpenHelper.getInstance(getContext()));
		this.networkCount = db.getNetworksCount();
	}

	@Override
	public View getView(final int position, View containerView, final ViewGroup parent) {
		TimeoStop currentStop = getItem(position);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(containerView == null) {
			containerView = inflater.inflate(R.layout.schedule_row, parent, false);
		}

		// Get references to the views
		FrameLayout view_line_id = (FrameLayout) containerView.findViewById(R.id.view_line_id);

		TextView lbl_line = (TextView) containerView.findViewById(R.id.lbl_line_id);
		TextView lbl_stop = (TextView) containerView.findViewById(R.id.lbl_stop_name);
		TextView lbl_direction = (TextView) containerView.findViewById(R.id.lbl_direction_name);

		LinearLayout view_schedule_container = (LinearLayout) containerView.findViewById(R.id.view_schedule_labels_container);

		ImageView img_stop_watched = (ImageView) containerView.findViewById(R.id.img_stop_watched);

		// Set line drawable. We have to set the colour on the background
		GradientDrawable lineDrawable = (GradientDrawable) view_line_id.getBackground();
		lineDrawable.setColor(Colors.getBrighterColor(Color.parseColor(currentStop.getLine().getColor())));

		lbl_line.setText(currentStop.getLine().getId());

		// Set stop text
		lbl_stop.setText(getContext().getResources().getString(R.string.stop_name, currentStop.getName()));

		// Set direction text
		lbl_direction.setText(getContext().getResources()
				.getString(R.string.direction_name, currentStop.getLine().getDirection().getName()));

		// Remove all existing schedules in that view
		view_schedule_container.removeAllViewsInLayout();

		// Add the new schedules one by one
		if(schedules.containsKey(currentStop) && schedules.get(currentStop) != null) {

			// If there are schedules
			if(schedules.get(currentStop).getSchedules() != null) {
				// Get the schedules for this stop
				List<TimeoSingleSchedule> currScheds = schedules.get(currentStop).getSchedules();

				for(TimeoSingleSchedule currSched : currScheds) {

					// We don't update from database all the time, so we can't figure this out by just updating everything.
					// If there is a bus coming, tell the stop that it's not watched anymore.
					// This won't work all the time, but it's not too bad.
					if(Calendar.getInstance().getTimeInMillis()
							> ScheduleTime.getNextDateForTime(currSched.getTime()).getTimeInMillis()) {
						currentStop.setWatched(false);
					}

					// Display the current schedule
					View singleScheduleView = inflater.inflate(R.layout.single_schedule_label, null);

					TextView lbl_schedule_time = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule);
					TextView lbl_schedule_direction = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule_direction);

					lbl_schedule_time.setText(currSched.getFormattedTime(getContext()));
					lbl_schedule_direction.setText(" — " + currSched.getDirection());

					view_schedule_container.addView(singleScheduleView);
				}

				if(currScheds.isEmpty()) {
					// If no schedules are available, add a fake one to inform the user
					view_schedule_container.addView(getEmptyScheduleLabel(inflater));
				}

				if(containerView.getAlpha() != 1.0F) {
					containerView.setAlpha(1.0F);

					AlphaAnimation alphaAnim = new AlphaAnimation(0.4F, 1.0f);
					alphaAnim.setDuration(500);
					containerView.startAnimation(alphaAnim);
				}
			}

		} else {
			// If we can't find the schedules we asked for in the hashmap, something went wrong. :c
			// It should be noted that it normally happens the first time the list is loaded, since no data was downloaded yet.
			Log.e(Utils.TAG, "missing stop schedule for " + currentStop +
					" (ref=" + currentStop.getReference() + "); ref outdated?");

			// Make the row look a bit translucent to make it stand out
			view_schedule_container.addView(getEmptyScheduleLabel(inflater));
			containerView.setAlpha(0.4F);
		}

		img_stop_watched.setVisibility((currentStop.isWatched()) ? View.VISIBLE : View.GONE);

		return containerView;
	}

	/**
	 * Instanciates and returns a view containing a label that says no stops are scheduled.
	 * Supposed to be used as a placeholder for an actual schedule time row.
	 */
	private View getEmptyScheduleLabel(LayoutInflater inflater) {
		View singleScheduleView = inflater.inflate(R.layout.single_schedule_label, null);
		TextView lbl_schedule_time = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule);
		lbl_schedule_time.setText(R.string.no_upcoming_stops);

		return singleScheduleView;
	}

	/**
	 * Fetches every stop schedule from the API and reloads everything.
	 */
	public void updateScheduleData() {
		// start refreshing schedules
		(new AsyncTask<Void, Void, Map<TimeoStop, TimeoStopSchedule>>() {

			@Override
			protected Map<TimeoStop, TimeoStopSchedule> doInBackground(Void... params) {
				try {
					// Get the schedules and put them in a list
					List<TimeoStopSchedule> schedulesList = TimeoRequestHandler.getMultipleSchedules(stops);
					Map<TimeoStop, TimeoStopSchedule> schedulesMap = new HashMap<>();

					for(TimeoStopSchedule schedule : schedulesList) {
						schedulesMap.put(schedule.getStop(), schedule);
					}

					// Check the number of outdated stops we tried to fetch
					nbOutdatedStops = TimeoRequestHandler.checkForOutdatedStops(stops, schedulesList);
					return schedulesMap;

				} catch(final Exception e) {
					e.printStackTrace();
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if(e instanceof TimeoBlockingMessageException) {
								((TimeoBlockingMessageException) e).getAlertMessage(getContext()).show();
							} else {
								String message;

								if(e instanceof TimeoException) {
									if(e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
										message = getContext().getString(R.string.error_toast_twisto_detailed,
												((TimeoException) e).getErrorCode(), e.getMessage());
									} else {
										message = getContext().getString(R.string.error_toast_twisto,
												((TimeoException) e).getErrorCode());
									}
								} else {
									message = getContext().getString(R.string.loading_error);
								}

								Snackbar.make(parentView, message, Snackbar.LENGTH_LONG)
										.setAction(R.string.error_retry, new View.OnClickListener() {

											@Override
											public void onClick(View view) {
												updateScheduleData();
											}

										})
										.show();
							}
						}

					});
				}

				return null;
			}

			@Override
			protected void onPostExecute(Map<TimeoStop, TimeoStopSchedule> scheduleMap) {
				schedules.clear();

				if(scheduleMap != null) {
					schedules.putAll(scheduleMap);
				}

				networkCount = db.getNetworksCount();

				notifyDataSetChanged();
				stopsListContainer.endRefresh((scheduleMap != null));
			}

		}).execute();
	}

	/**
	 * Check if there's a mismatch between the number of bus stops requested and the
	 * number of schedules we got back from the API.
	 *
	 * @return 0 if everything is okay, otherwise the number of stops we didn't get back the data for.
	 */
	public int checkSchedulesMismatchCount() {
		return nbOutdatedStops;
	}

}
