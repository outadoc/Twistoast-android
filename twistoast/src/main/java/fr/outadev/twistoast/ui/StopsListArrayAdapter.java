/*
 * Twistoast - StopsListArrayAdapter
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

package fr.outadev.twistoast.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.model.TimeoBlockingMessageException;
import fr.outadev.android.timeo.model.TimeoSingleSchedule;
import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.android.timeo.model.TimeoStopSchedule;
import fr.outadev.twistoast.IStopsListContainer;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.Utils;
import fr.outadev.twistoast.database.TwistoastDatabase;

/**
 * An array adapter for the main list of bus stops.
 *
 * @author outadoc
 */
public class StopsListArrayAdapter extends ArrayAdapter<TimeoStop> {

	private final IStopsListContainer stopsListContainer;
	private final Activity activity;

	private final List<TimeoStop> stops;
	private final Map<TimeoStop, TimeoStopSchedule> schedules;
	private final SparseArray<String> networks;

	private int networkCount = 0;

	public StopsListArrayAdapter(Activity activity, int resource, List<TimeoStop> stops,
	                             IStopsListContainer stopsListContainer) {
		super(activity, resource, stops);

		this.activity = activity;
		this.stops = stops;
		this.stopsListContainer = stopsListContainer;
		this.schedules = new HashMap<>();
		this.networks = TimeoRequestHandler.getNetworksList();
		this.networkCount = (new TwistoastDatabase(getContext())).getNetworksCount();
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		TimeoStop currentItem = getItem(position);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(convertView == null) {
			convertView = inflater.inflate(R.layout.schedule_row, parent, false);
		}

		// get all the stuff in it that we'll have to modify
		FrameLayout view_line_id = (FrameLayout) convertView.findViewById(R.id.view_line_id);

		TextView lbl_line = (TextView) convertView.findViewById(R.id.lbl_line_id);
		TextView lbl_stop = (TextView) convertView.findViewById(R.id.lbl_stop_name);
		TextView lbl_direction = (TextView) convertView.findViewById(R.id.lbl_direction_name);

		LinearLayout view_schedule_container = (LinearLayout) convertView.findViewById(R.id.view_schedule_labels_container);
		LinearLayout view_traffic_message = (LinearLayout) convertView.findViewById(R.id.view_traffic_message);

		/*
		TextView lbl_message_title = (TextView) convertView.findViewById(R.id.lbl_message_title);
		TextView lbl_message_body = (TextView) convertView.findViewById(R.id.lbl_message_body);


		// set and make the message labels visible if necessary
		if(currentItem.getMessageTitle() != null && currentItem.getMessageBody() != null
				&& !currentItem.getMessageBody().isEmpty() && !currentItem.getMessageTitle().isEmpty()) {
			lbl_message_title.setText(currentItem.getMessageTitle());
			lbl_message_body.setText(currentItem.getMessageBody());

			view_traffic_message.setVisibility(View.VISIBLE);
		} else {
			view_traffic_message.setVisibility(View.GONE);
		}*/

		// line
		GradientDrawable lineDrawable = (GradientDrawable) view_line_id.getBackground();
		lineDrawable.setColor(Color.parseColor(currentItem.getLine().getColor()));

		lbl_line.setText(currentItem.getLine().getId());

		// stop
		lbl_stop.setText(getContext().getResources().getString(R.string.stop_name, currentItem.getName()));

		// direction
		lbl_direction.setText(getContext().getResources()
				.getString(R.string.direction_name, currentItem.getLine().getDirection().getName()));

		//remove all existing schedules in that view
		view_schedule_container.removeAllViewsInLayout();
		view_schedule_container.setVisibility(View.GONE);

		//add the new schedules one by one
		if(schedules.containsKey(currentItem) && schedules.get(currentItem) != null) {
			if(schedules.get(currentItem).getSchedules() != null) {
				List<TimeoSingleSchedule> currScheds = schedules.get(currentItem).getSchedules();

				for(TimeoSingleSchedule currSched : currScheds) {
					View singleScheduleView = inflater.inflate(R.layout.single_schedule_label, null);

					TextView lbl_schedule_time = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule);
					TextView lbl_schedule_direction = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule_direction);

					lbl_schedule_time.setText(currSched.getFormattedTime(getContext()));
					lbl_schedule_direction.setText(" — " + currSched.getDirection());

					view_schedule_container.addView(singleScheduleView);
				}

				if(currScheds.size() > 0) {
					view_schedule_container.setVisibility(View.VISIBLE);
				}
			}
		} else {
			//if we can't find the schedules we asked for in the hashmap, something went wrong. :c
			Log.e(Utils.TAG, "missing stop schedule for " + currentItem + " (ref=" + currentItem.getReference() + "); ref " +
					"outdated?");
		}

		view_traffic_message.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopsListContainer.loadFragmentFromDrawerPosition(3);
			}

		});

		return convertView;
	}

	/**
	 * Fetches from the API and reloads the schedules.
	 */
	public void updateScheduleData() {
		// start refreshing schedules
		(new AsyncTask<Void, Void, Map<TimeoStop, TimeoStopSchedule>>() {

			@Override
			protected Map<TimeoStop, TimeoStopSchedule> doInBackground(Void... params) {
				try {
					List<TimeoStopSchedule> schedulesList = TimeoRequestHandler.getMultipleSchedules(stops);
					Map<TimeoStop, TimeoStopSchedule> schedulesMap = new HashMap<TimeoStop, TimeoStopSchedule>();

					for(TimeoStopSchedule schedule : schedulesList) {
						schedulesMap.put(schedule.getStop(), schedule);
					}

					return schedulesMap;
				} catch(final Exception e) {
					e.printStackTrace();
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if(e instanceof TimeoBlockingMessageException) {
								((TimeoBlockingMessageException) e).getAlertMessage(getContext()).show();
							} else {
								Snackbar.with(getContext())
										.text(R.string.loading_error)
										.actionLabel("Retry")
										.actionColorResource(R.color.colorAccent)
										.actionListener(new ActionClickListener() {

											@Override
											public void onActionClicked() {
												updateScheduleData();
											}

										})
										.show(activity);
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

				networkCount = (new TwistoastDatabase(getContext())).getNetworksCount();

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
		int delta = stops.size() - schedules.size();
		if(delta < 0) {
			throw new RuntimeException("Got more data back from the API than expected.");
		}
		return delta;
	}

}
