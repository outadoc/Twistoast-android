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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.outadev.android.transport.timeo.ITimeoRequestHandler;
import fr.outadev.android.transport.timeo.TimeoBlockingMessageException;
import fr.outadev.android.transport.timeo.TimeoException;
import fr.outadev.android.transport.timeo.TimeoRequestHandler;
import fr.outadev.android.transport.timeo.TimeoSingleSchedule;
import fr.outadev.android.transport.timeo.TimeoStop;
import fr.outadev.android.transport.timeo.TimeoStopSchedule;
import fr.outadev.twistoast.background.BackgroundTasksManager;

/**
 * An array adapter for the main list of bus stops.
 *
 * @author outadoc
 */
public class RecyclerAdapterRealtime extends RecyclerView.Adapter<RecyclerAdapterRealtime.ViewHolder> implements
        IRecyclerAdapterAccess {

    public final static String TAG = RecyclerAdapterRealtime.class.getSimpleName();

    public static final int NB_SCHEDULES_DISPLAYED = 2;

    private final View mParentView;
    private final Activity mActivity;

    private final Database mDatabase;
    private final ConfigurationManager mConfig;
    private final TimeoStopReferenceUpdater mReferenceUpdater;
    private final ITimeoRequestHandler mRequestHandler;

    private final IStopsListContainer mStopsListContainer;

    private final List<TimeoStop> mStopsList;
    private final Map<TimeoStop, TimeoStopSchedule> mSchedules;

    private int mNetworkCount = 0;

    private ViewHolder.IOnLongClickListener mLongClickListener = new ViewHolder.IOnLongClickListener() {

        @Override
        public boolean onLongClick(View view, int position) {
            if (!mStopsListContainer.isRefreshing()) {
                final TimeoStop currentStop = mStopsList.get(position);

                // Menu items
                String contextualMenuItems[] = new String[]{
                        view.getContext().getString(R.string.stop_action_delete),
                        view.getContext().getString((!currentStop.isWatched()) ? R.string.stop_action_watch : R.string
                                .stop_action_unwatch)
                };

                // Build the long click contextual menu
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(contextualMenuItems, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {
                            deleteStopAction(currentStop, which);
                        } else if (which == 1 && !currentStop.isWatched()) {
                            startWatchingStopAction(currentStop);
                        } else if (which == 1) {
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
            mDatabase.deleteStop(stop);
            mStopsList.remove(stop);

            if (stop.isWatched()) {
                mDatabase.stopWatchingStop(stop);
                stop.setWatched(false);
            }

            if (mStopsList.isEmpty()) {
                mStopsListContainer.setNoContentViewVisible(true);
            }

            notifyDataSetChanged();

            Snackbar.make(mParentView, R.string.confirm_delete_success, Snackbar.LENGTH_LONG)
                    .setAction(R.string.cancel_stop_deletion, new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            Log.i(TAG, "restoring stop " + stop);

                            mDatabase.addStopToDatabase(stop);
                            mStopsList.add(position, stop);
                            notifyDataSetChanged();
                        }

                    })
                    .show();
        }

        private void startWatchingStopAction(final TimeoStop stop) {
            // We wish to get notifications about this upcoming stop
            mDatabase.addToWatchedStops(stop);
            stop.setWatched(true);
            notifyDataSetChanged();

            // Turn the notifications on
            BackgroundTasksManager.enableStopAlarmJob(getActivity().getApplicationContext());

            Snackbar.make(mParentView, mParentView.getContext().getString(R.string.notifs_enable_toast, stop.getName()), Snackbar
                    .LENGTH_LONG)
                    .setAction(R.string.cancel_stop_deletion, new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            mDatabase.stopWatchingStop(stop);
                            stop.setWatched(false);
                            notifyDataSetChanged();

                            // Turn the notifications back off if necessary
                            if (mDatabase.getWatchedStopsCount() == 0) {
                                BackgroundTasksManager.disableStopAlarmJob(getActivity().getApplicationContext());
                            }
                        }

                    })
                    .show();
        }

        private void stopWatchingStopAction(TimeoStop stop) {
            // JUST STOP THESE NOTIFICATIONS ALREADY GHGHGHBLBLBL
            mDatabase.stopWatchingStop(stop);
            stop.setWatched(false);
            notifyDataSetChanged();

            // Turn the notifications back off if necessary
            if (mDatabase.getWatchedStopsCount() == 0) {
                BackgroundTasksManager.disableStopAlarmJob(getActivity().getApplicationContext());
            }

            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context
                    .NOTIFICATION_SERVICE);
            notificationManager.cancel(Integer.valueOf(stop.getId()));

            Snackbar.make(mParentView, mParentView.getContext().getString(R.string.notifs_disable_toast, stop.getName()), Snackbar
                    .LENGTH_LONG)
                    .show();
        }

    };

    public RecyclerAdapterRealtime(Activity activity, List<TimeoStop> stops, IStopsListContainer stopsListContainer, View
            parentView) {
        mActivity = activity;
        mStopsList = stops;
        mStopsListContainer = stopsListContainer;
        mParentView = parentView;
        mSchedules = new HashMap<>();
        mDatabase = new Database(DatabaseOpenHelper.getInstance(activity));
        mNetworkCount = mDatabase.getNetworksCount();
        mReferenceUpdater = new TimeoStopReferenceUpdater(getActivity());
        mConfig = new ConfigurationManager(activity);
        mRequestHandler = new TimeoRequestHandler();
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
                    List<TimeoStopSchedule> schedulesList = mRequestHandler.getMultipleSchedules(mStopsList);
                    Map<TimeoStop, TimeoStopSchedule> schedulesMap = new HashMap<>();

                    for (TimeoStopSchedule schedule : schedulesList) {
                        schedulesMap.put(schedule.getStop(), schedule);
                    }

                    int outdated = mRequestHandler.checkForOutdatedStops(mStopsList, schedulesList);

                    // If there are outdated reference numbers, update those stops
                    if (outdated > 0) {
                        Log.e(TAG, "Found " + outdated + " stops, trying to update references");
                        mReferenceUpdater.updateAllStopReferences(mStopsList, null);

                        // Reload with the updated stops
                        schedulesList = mRequestHandler.getMultipleSchedules(mStopsList);
                        schedulesMap = new HashMap<>();

                        for (TimeoStopSchedule schedule : schedulesList) {
                            schedulesMap.put(schedule.getStop(), schedule);
                        }
                    }

                    return schedulesMap;

                } catch (final Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // It's it's a blocking message, display it in a dialog
                            if (e instanceof TimeoBlockingMessageException) {
                                ((TimeoBlockingMessageException) e).getAlertMessage(getActivity()).show();
                            } else {
                                String message;

                                // If the error is a NavitiaException, we'll use a special formatting string
                                if (e instanceof TimeoException) {
                                    TimeoException e1 = (TimeoException) e;

                                    // If there are details to the error, display them. Otherwise, only display the error code
                                    if (e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
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

                                Snackbar.make(mParentView, message, Snackbar.LENGTH_LONG)
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
                mSchedules.clear();

                if (scheduleMap != null) {
                    mSchedules.putAll(scheduleMap);
                }

                mNetworkCount = mDatabase.getNetworksCount();

                notifyDataSetChanged();
                mStopsListContainer.endRefresh((scheduleMap != null));
            }

        }).execute();
    }

    @Override
    public RecyclerAdapterRealtime.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_schedule_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapterRealtime.ViewHolder view, final int position) {
        // Get the stop we're inflating
        TimeoStop currentStop = mStopsList.get(position);

        view.mLineDrawable.setColor(Colors.getBrighterColor(Color.parseColor(currentStop.getLine().getColor())));

        view.mLblLine.setText(currentStop.getLine().getId());
        view.mLblStop.setText(view.mLblStop.getContext().getString(R.string.stop_name, currentStop.getName()));
        view.mLblDirection.setText(view.mLblDirection.getContext().getString(R.string.direction_name, currentStop.getLine()
                .getDirection().getName()));

        // Clear labels
        for (int i = 0; i < NB_SCHEDULES_DISPLAYED; i++) {
            view.mLblScheduleTime[i].setText("");
            view.mLblScheduleDirection[i].setText("");
        }

        // Add the new schedules one by one
        if (mSchedules.containsKey(currentStop) && mSchedules.get(currentStop) != null) {

            // If there are schedules
            if (mSchedules.get(currentStop).getSchedules() != null) {
                // Get the schedules for this stop
                List<TimeoSingleSchedule> currScheds = mSchedules.get(currentStop).getSchedules();

                for (int i = 0; i < currScheds.size() && i < NB_SCHEDULES_DISPLAYED; i++) {
                    TimeoSingleSchedule currSched = currScheds.get(i);

                    // We don't update from database all the time, so we can't figure this out by just updating everything.
                    // If there is a bus coming, tell the stop that it's not watched anymore.
                    // This won't work all the time, but it's not too bad.
                    if (currSched.getScheduleTime().isBeforeNow()) {
                        currentStop.setWatched(false);
                    }

                    view.mLblScheduleTime[i].setText(TimeFormatter.formatTime(view.mLblScheduleTime[i].getContext(), currSched.getScheduleTime()));
                    view.mLblScheduleDirection[i].setText(" — " + currSched.getDirection());
                }

                if (currScheds.isEmpty()) {
                    // If no schedules are available, add a fake one to inform the user
                    view.mLblScheduleTime[0].setText(R.string.no_upcoming_stops);
                }

                // Fade in the row!
                if (view.mContainer.getAlpha() != 1.0F) {
                    view.mContainer.setAlpha(1.0F);

                    AlphaAnimation alphaAnim = new AlphaAnimation(0.4F, 1.0f);
                    alphaAnim.setDuration(500);
                    view.mContainer.startAnimation(alphaAnim);
                }
            }

        } else {
            // If we can't find the schedules we asked for in the hashmap, something went wrong. :c
            // It should be noted that it normally happens the first time the list is loaded, since no data was downloaded yet.
            Log.e(TAG, "missing stop schedule for " + currentStop +
                    " (ref=" + currentStop.getReference() + "); ref outdated?");

            // Make the row look a bit translucent to make it stand out
            view.mLblScheduleTime[0].setText(R.string.no_upcoming_stops);
            view.mContainer.setAlpha(0.4F);
        }

        view.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mLongClickListener.onLongClick(v, position);
            }
        });

        view.mImgStopWatched.setVisibility((currentStop.isWatched()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mStopsList.size();
    }

    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public boolean shouldItemHaveSeparator(int position) {
        // If it's the last item, no separator
        if (position < 0 || position == mStopsList.size() - 1) {
            return false;
        }

        TimeoStop item = mStopsList.get(position);
        TimeoStop nextItem = mStopsList.get(position + 1);

        Database.SortBy criteria = mConfig.getListSortOrder();

        if (criteria == Database.SortBy.STOP) {
            // If the next item's stop is the same as this one, don't draw a separator either
            return !(item.getId().equals(nextItem.getId()) || item.getName().equals(nextItem.getName()));
        } else {
            // If the next item's line is the same as this one, don't draw a separator either
            return !item.getLine().getId().equals(nextItem.getLine().getId());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout mContainer;
        public FrameLayout mViewLineId;

        public TextView mLblLine;
        public TextView mLblStop;
        public TextView mLblDirection;
        public LinearLayout mViewScheduleContainer;
        public ImageView mImgStopWatched;
        public GradientDrawable mLineDrawable;

        public TextView[] mLblScheduleTime = new TextView[NB_SCHEDULES_DISPLAYED];
        public TextView[] mLblScheduleDirection = new TextView[NB_SCHEDULES_DISPLAYED];

        public ViewHolder(View v) {
            super(v);

            LayoutInflater inflater = LayoutInflater.from(v.getContext());
            mContainer = (LinearLayout) v;

            // Get references to the views
            mViewLineId = (FrameLayout) v.findViewById(R.id.rowLineIdContainer);

            mLblLine = (TextView) v.findViewById(R.id.rowLineId);
            mLblStop = (TextView) v.findViewById(R.id.rowStopName);
            mLblDirection = (TextView) v.findViewById(R.id.rowDirectionName);

            mViewScheduleContainer = (LinearLayout) v.findViewById(R.id.view_schedule_labels_container);
            mImgStopWatched = (ImageView) v.findViewById(R.id.img_stop_watched);
            mLineDrawable = (GradientDrawable) mViewLineId.getBackground();

            for (int i = 0; i < NB_SCHEDULES_DISPLAYED; i++) {
                // Display the current schedule
                View singleScheduleView = inflater.inflate(R.layout.view_single_schedule_label, null);

                mLblScheduleTime[i] = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule);
                mLblScheduleDirection[i] = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule_direction);

                mViewScheduleContainer.addView(singleScheduleView);
            }
        }

        public interface IOnLongClickListener {

            boolean onLongClick(View view, int position);
        }

    }

}
