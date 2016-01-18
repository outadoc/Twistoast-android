/*
 * Twistoast - RecyclerAdapterRealtime
 * Copyright (C) 2013-2016 Baptiste Candellier
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
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.outadev.android.timeo.TimeoBlockingMessageException;
import fr.outadev.android.timeo.TimeoException;
import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoSingleSchedule;
import fr.outadev.android.timeo.TimeoStop;
import fr.outadev.android.timeo.TimeoStopSchedule;
import fr.outadev.twistoast.background.NextStopAlarmReceiver;

/**
 * An array adapter for the main list of bus stops.
 *
 * @author outadoc
 */
public class RecyclerAdapterRealtime extends RecyclerView.Adapter<RecyclerAdapterRealtime.ViewHolder> implements IRecyclerAdapterAccess {

	public static final int NB_SCHEDULES_DISPLAYED = 2;

	private final IStopsListContainer stopsListContainer;
	private final View parentView;

	private final Activity activity;
	private final Database db;

	private final List<TimeoStop> stops;
	private final Map<TimeoStop, TimeoStopSchedule> schedules;
	private final SparseArray<String> networks;

	private int networkCount = 0;
	private int nbOutdatedStops = 0;

	private ViewHolder.IOnLongClickListener clickListener = new ViewHolder.IOnLongClickListener() {

		@Override
		public boolean onLongClick(View view, int position) {
			if(!stopsListContainer.isRefreshing()) {
				final TimeoStop currentStop = stops.get(position);

				// Menu items
				String contextualMenuItems[] = new String[]{
						view.getContext().getString(R.string.stop_action_delete),
						view.getContext().getString((!currentStop.isWatched()) ? R.string.stop_action_watch : R.string.stop_action_unwatch)
				};

				// Build the long click contextual menu
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setItems(contextualMenuItems, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						if(which == 0) {
							deleteStopAction(currentStop, which);
						} else if(which == 1 && !currentStop.isWatched()) {
							startWatchingStopAction(currentStop);
						} else if(which == 1) {
							stopWatchingStopAction(currentStop);
						}
					}

				});

				builder.show();
			}

			return true;
		}

		private void deleteStopAction(final TimeoStop stop, final int position) {
			// Remove from the database and the interface
			db.deleteStop(stop);
			stops.remove(stop);

			if(stop.isWatched()) {
				db.stopWatchingStop(stop);
				stop.setWatched(false);
			}

			if(stops.isEmpty()) {
				stopsListContainer.setNoContentViewVisible(true);
			}

			notifyDataSetChanged();

			Snackbar.make(parentView, R.string.confirm_delete_success, Snackbar.LENGTH_LONG)
					.setAction(R.string.cancel_stop_deletion, new View.OnClickListener() {

						@Override
						public void onClick(View view) {
							Log.i(Utils.TAG, "restoring stop " + stop);

							db.addStopToDatabase(stop);
							stops.add(position, stop);
							notifyDataSetChanged();
						}

					})
					.show();
		}

		private void startWatchingStopAction(final TimeoStop stop) {
			// We wish to get notifications about this upcoming stop
			db.addToWatchedStops(stop);
			stop.setWatched(true);
			notifyDataSetChanged();

			// Turn the notifications on
			NextStopAlarmReceiver.enable(getActivity().getApplicationContext());

			Snackbar.make(parentView, parentView.getContext().getString(R.string.notifs_enable_toast, stop.getName()), Snackbar.LENGTH_LONG)
					.setAction(R.string.cancel_stop_deletion, new View.OnClickListener() {

						@Override
						public void onClick(View view) {
							db.stopWatchingStop(stop);
							stop.setWatched(false);
							notifyDataSetChanged();

							// Turn the notifications back off if necessary
							if(db.getWatchedStopsCount() == 0) {
								NextStopAlarmReceiver.disable(getActivity().getApplicationContext());
							}
						}

					})
					.show();
		}

		private void stopWatchingStopAction(TimeoStop stop) {
			// JUST STOP THESE NOTIFICATIONS ALREADY GHGHGHBLBLBL
			db.stopWatchingStop(stop);
			stop.setWatched(false);
			notifyDataSetChanged();

			// Turn the notifications back off if necessary
			if(db.getWatchedStopsCount() == 0) {
				NextStopAlarmReceiver.disable(getActivity().getApplicationContext());
			}

			NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(Integer.valueOf(stop.getId()));

			Snackbar.make(parentView, parentView.getContext().getString(R.string.notifs_disable_toast, stop.getName()), Snackbar.LENGTH_LONG)
					.show();
		}

	};

	public RecyclerAdapterRealtime(Activity activity, List<TimeoStop> stops, IStopsListContainer stopsListContainer, View parentView) {
		this.activity = activity;
		this.stops = stops;
		this.stopsListContainer = stopsListContainer;
		this.parentView = parentView;
		this.schedules = new HashMap<>();
		this.networks = TimeoRequestHandler.getNetworksList();
		this.db = new Database(DatabaseOpenHelper.getInstance(activity));
		this.networkCount = db.getNetworksCount();
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
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// It's it's a blocking message, display it in a dialog
							if(e instanceof TimeoBlockingMessageException) {
								((TimeoBlockingMessageException) e).getAlertMessage(getActivity()).show();
							} else {
								String message;

								// If the error is a TimeoException, we'll use a special formatting string
								if(e instanceof TimeoException) {
									TimeoException e1 = (TimeoException) e;

									// If there are details to the error, display them. Otherwise, only display the error code
									if(e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
										message = getActivity().getString(R.string.error_toast_twisto_detailed,
												e1.getErrorCode(), e.getMessage());
									} else {
										message = getActivity().getString(R.string.error_toast_twisto,
												e1.getErrorCode());
									}
								} else {
									// If it's a simple error, just display a generic error message
									message = getActivity().getString(R.string.loading_error);
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

	@Override
	public RecyclerAdapterRealtime.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_schedule_row, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(RecyclerAdapterRealtime.ViewHolder view, final int position) {
		// Get the stop we're inflating
		TimeoStop currentStop = stops.get(position);

		view.lineDrawable.setColor(Colors.getBrighterColor(Color.parseColor(currentStop.getLine().getColor())));

		view.lbl_line.setText(currentStop.getLine().getId());
		view.lbl_stop.setText(view.lbl_stop.getContext().getString(R.string.stop_name, currentStop.getName()));
		view.lbl_direction.setText(view.lbl_direction.getContext().getString(R.string.direction_name, currentStop.getLine().getDirection().getName()));

		// Clear labels
		for(int i = 0; i < NB_SCHEDULES_DISPLAYED; i++) {
			view.lbl_schedule_time[i].setText("");
			view.lbl_schedule_direction[i].setText("");
		}

		// Add the new schedules one by one
		if(schedules.containsKey(currentStop) && schedules.get(currentStop) != null) {

			// If there are schedules
			if(schedules.get(currentStop).getSchedules() != null) {
				// Get the schedules for this stop
				List<TimeoSingleSchedule> currScheds = schedules.get(currentStop).getSchedules();

				for(int i = 0; i < currScheds.size() && i < NB_SCHEDULES_DISPLAYED; i++) {
					TimeoSingleSchedule currSched = currScheds.get(i);

					// We don't update from database all the time, so we can't figure this out by just updating everything.
					// If there is a bus coming, tell the stop that it's not watched anymore.
					// This won't work all the time, but it's not too bad.
					if(Calendar.getInstance().getTimeInMillis()	> currSched.getTime().getTimeInMillis()) {
						currentStop.setWatched(false);
					}

					view.lbl_schedule_time[i].setText(currSched.getFormattedTime(view.lbl_schedule_time[i].getContext()));
					view.lbl_schedule_direction[i].setText(" — " + currSched.getDirection());
				}

				if(currScheds.isEmpty()) {
					// If no schedules are available, add a fake one to inform the user
					view.lbl_schedule_time[0].setText(R.string.no_upcoming_stops);
				}

				// Fade in the row!
				if(view.container.getAlpha() != 1.0F) {
					view.container.setAlpha(1.0F);

					AlphaAnimation alphaAnim = new AlphaAnimation(0.4F, 1.0f);
					alphaAnim.setDuration(500);
					view.container.startAnimation(alphaAnim);
				}
			}

		} else {
			// If we can't find the schedules we asked for in the hashmap, something went wrong. :c
			// It should be noted that it normally happens the first time the list is loaded, since no data was downloaded yet.
			Log.e(Utils.TAG, "missing stop schedule for " + currentStop +
					" (ref=" + currentStop.getReference() + "); ref outdated?");

			// Make the row look a bit translucent to make it stand out
			view.lbl_schedule_time[0].setText(R.string.no_upcoming_stops);
			view.container.setAlpha(0.4F);
		}

		view.container.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return clickListener.onLongClick(v, position);
			}
		});

		view.img_stop_watched.setVisibility((currentStop.isWatched()) ? View.VISIBLE : View.GONE);
	}

	@Override
	public int getItemCount() {
		return stops.size();
	}

	public Activity getActivity() {
		return activity;
	}

	@Override
	public boolean shouldItemHaveSeparator(int position) {
		// If it's the last item, no separator
		if(position == stops.size() - 1) {
			return false;
		}

		TimeoStop item = stops.get(position);
		TimeoStop nextItem = stops.get(position + 1);

		// If the next item's line is the same as this one, don't draw a separator either
		return !item.getLine().getId().equals(nextItem.getLine().getId());

	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public LinearLayout container;
		public FrameLayout view_line_id;

		public TextView lbl_line;
		public TextView lbl_stop;
		public TextView lbl_direction;
		public LinearLayout view_schedule_container;
		public ImageView img_stop_watched;
		public GradientDrawable lineDrawable;

		public TextView[] lbl_schedule_time = new TextView[NB_SCHEDULES_DISPLAYED];
		public TextView[] lbl_schedule_direction = new TextView[NB_SCHEDULES_DISPLAYED];

		public ViewHolder(View v) {
			super(v);

			LayoutInflater inflater = LayoutInflater.from(v.getContext());
			container = (LinearLayout) v;

			// Get references to the views
			view_line_id = (FrameLayout) v.findViewById(R.id.view_line_id);

			lbl_line = (TextView) v.findViewById(R.id.lbl_line_id);
			lbl_stop = (TextView) v.findViewById(R.id.lbl_stop_name);
			lbl_direction = (TextView) v.findViewById(R.id.lbl_direction_name);

			view_schedule_container = (LinearLayout) v.findViewById(R.id.view_schedule_labels_container);
			img_stop_watched = (ImageView) v.findViewById(R.id.img_stop_watched);
			lineDrawable = (GradientDrawable) view_line_id.getBackground();

			for(int i = 0; i < NB_SCHEDULES_DISPLAYED; i++) {
				// Display the current schedule
				View singleScheduleView = inflater.inflate(R.layout.frag_single_schedule_label, null);

				lbl_schedule_time[i] = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule);
				lbl_schedule_direction[i] = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule_direction);

				view_schedule_container.addView(singleScheduleView);
			}
		}

		public interface IOnLongClickListener {

			boolean onLongClick(View view, int position);
		}

	}

}
