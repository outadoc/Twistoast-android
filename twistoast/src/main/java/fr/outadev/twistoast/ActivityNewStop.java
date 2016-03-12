/*
 * Twistoast - ActivityNewStop
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

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.outadev.android.transport.timeo.TimeoBlockingMessageException;
import fr.outadev.android.transport.timeo.TimeoException;
import fr.outadev.android.transport.timeo.TimeoIDNameObject;
import fr.outadev.android.transport.timeo.TimeoLine;
import fr.outadev.android.transport.timeo.TimeoRequestHandler;
import fr.outadev.android.transport.timeo.TimeoSingleSchedule;
import fr.outadev.android.transport.timeo.TimeoStop;
import fr.outadev.android.transport.timeo.TimeoStopSchedule;

/**
 * Activity that allows the user to add a bus stop to the app.
 *
 * @author outadoc
 */
public class ActivityNewStop extends ThemedActivity {

    public static final int NO_STOP_ADDED = 0;
    public static final int STOP_ADDED = 1;

    private Spinner mSpinLine;
    private Spinner mSpinDirection;
    private Spinner mSpinStop;

    private List<TimeoLine> mLineList;
    private List<TimeoIDNameObject> mDirectionList;
    private List<TimeoStop> mStopList;

    private List<TimeoLine> mFilteredLineList;

    private ArrayAdapter<TimeoLine> mLineAdapter;
    private ArrayAdapter<TimeoIDNameObject> mDirectionAdapter;
    private ArrayAdapter<TimeoStop> mStopAdapter;

    private TextView mLblLine;
    private FrameLayout mViewLineId;
    private TextView mLblStop;
    private TextView mLblDirection;

    private LinearLayout mViewScheduleContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private MenuItem mItemNext;

    private Database mDatabaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup everything
        setContentView(R.layout.activity_new_stop);
        setResult(NO_STOP_ADDED);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);

        mDatabaseHandler = new Database(DatabaseOpenHelper.getInstance(this));

        // get all the UI elements we'll need in the future

        // spinners (dropdown menus)
        mSpinLine = (Spinner) findViewById(R.id.spin_line);
        mSpinDirection = (Spinner) findViewById(R.id.spin_direction);
        mSpinStop = (Spinner) findViewById(R.id.spin_stop);

        //lists
        mLineList = new ArrayList<>();
        mDirectionList = new ArrayList<>();
        mStopList = new ArrayList<>();

        mFilteredLineList = new ArrayList<>(mLineList);

        // labels
        mLblLine = (TextView) findViewById(R.id.lbl_line_id);
        mLblStop = (TextView) findViewById(R.id.lbl_stop_name);
        mLblDirection = (TextView) findViewById(R.id.lbl_direction_name);

        // line view (to set its background color)
        mViewLineId = (FrameLayout) findViewById(R.id.view_line_id);

        mViewScheduleContainer = (LinearLayout) findViewById(R.id.view_schedule_labels_container);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.twisto_primary, R.color.twisto_secondary,
                R.color.twisto_primary, R.color.twisto_secondary);
        mSwipeRefreshLayout.setRefreshing(true);

        mSpinLine.setEnabled(false);
        mSpinDirection.setEnabled(false);
        mSpinStop.setEnabled(false);

        //setup spinners here
        setupLineSpinner();
        setupDirectionSpinner();
        setupStopSpinner();

        getLinesFromAPI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_stop, menu);
        mItemNext = menu.findItem(R.id.action_ok);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ok:
                // add the current stop to the database
                registerStopToDatabase();
                return true;
            case android.R.id.home:
                // go back
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialises, sets up the even listeners, and populates the line spinner.
     */
    public void setupLineSpinner() {
        mLineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mFilteredLineList);
        mLineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinLine.setAdapter(mLineAdapter);

        // when a line has been selected
        mSpinLine.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                // set loading labels
                mLblLine.setText("");
                mLblDirection.setText(getResources().getString(R.string.loading_data));
                mLblStop.setText(getResources().getString(R.string.loading_data));

                mViewScheduleContainer.removeAllViewsInLayout();
                mViewScheduleContainer.setVisibility(View.GONE);

                mSpinStop.setEnabled(false);

                if (mItemNext != null) {
                    mItemNext.setEnabled(false);
                }

                // get the selected line
                TimeoLine item = getCurrentLine();

                if (item != null && item.getId() != null) {
                    // set the line view
                    mLblLine.setText(item.getId());

                    GradientDrawable lineDrawable = (GradientDrawable) mViewLineId.getBackground();
                    lineDrawable.setColor(Colors.getBrighterColor(Color.parseColor(item.getColor())));

                    mSpinDirection.setEnabled(true);

                    // adapt the size based on the size of the line ID
                    if (mLblLine.getText().length() > 3) {
                        mLblLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    } else if (mLblLine.getText().length() > 2) {
                        mLblLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                    } else {
                        mLblLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                    }

                    mDirectionList.clear();
                    mDirectionList.addAll(getDirectionsList());
                    setupDirectionSpinner();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    /**
     * Initialises, sets up the even listeners, and populates the direction spinner.
     */
    public void setupDirectionSpinner() {
        mDirectionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mDirectionList);
        mDirectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinDirection.setAdapter(mDirectionAdapter);

        // when a line has been selected
        mSpinDirection.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                // set loading labels
                mLblDirection.setText(getResources().getString(R.string.loading_data));
                mViewScheduleContainer.removeAllViewsInLayout();
                mViewScheduleContainer.setVisibility(View.GONE);

                mItemNext.setEnabled(false);
                mSpinStop.setEnabled(false);

                if (getCurrentLine() != null && getCurrentDirection() != null && getCurrentLine().getId() != null
                        && getCurrentDirection().getId() != null) {
                    mLblDirection.setText(getResources().getString(R.string.direction_name, getCurrentDirection().getName()));

                    (new AsyncTask<Void, Void, List<TimeoStop>>() {

                        @Override
                        protected List<TimeoStop> doInBackground(Void... voids) {
                            try {
                                getCurrentLine().setDirection(getCurrentDirection());
                                return TimeoRequestHandler.getStops(getCurrentLine());
                            } catch (Exception e) {
                                handleAsyncExceptions(e);
                            }

                            return null;
                        }
                        
                        @Override
                        protected void onPreExecute() {
                            mSwipeRefreshLayout.setEnabled(true);
                            mSwipeRefreshLayout.setRefreshing(true);
                        }





                        @Override
                        protected void onPostExecute(List<TimeoStop> timeoStops) {
                            if (timeoStops != null) {
                                mSpinStop.setEnabled(true);

                                mStopList.clear();
                                mStopList.addAll(timeoStops);
                                setupStopSpinner();
                            }
                        }

                    }).execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    /**
     * Initialises, sets up the even listeners, and populates the stop spinner.
     */
    public void setupStopSpinner() {
        mStopAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mStopList);
        mStopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinStop.setAdapter(mStopAdapter);

        // when a stop has been selected
        mSpinStop.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                mLblStop.setText(getResources().getString(R.string.loading_data));
                mViewScheduleContainer.removeAllViewsInLayout();
                mViewScheduleContainer.setVisibility(View.GONE);

                TimeoIDNameObject stop = getCurrentStop();
                mItemNext.setEnabled(true);

                if (stop != null && stop.getId() != null) {
                    mLblStop.setText(getResources().getString(R.string.stop_name, stop.getName()));
                    updateSchedulePreview();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    public void updateSchedulePreview() {
        (new AsyncTask<Void, Void, TimeoStopSchedule>() {

            @Override
            protected void onPreExecute() {
                mSwipeRefreshLayout.setEnabled(true);
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected TimeoStopSchedule doInBackground(Void... voids) {
                try {
                    return TimeoRequestHandler.getSingleSchedule(getCurrentStop());
                } catch (Exception e) {
                    handleAsyncExceptions(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(TimeoStopSchedule schedule) {
                LayoutInflater inflater = (LayoutInflater) ActivityNewStop.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                mSwipeRefreshLayout.setEnabled(false);
                mSwipeRefreshLayout.setRefreshing(false);

                if (schedule != null) {
                    List<TimeoSingleSchedule> schedList = schedule.getSchedules();

                    // set the schedule labels, if we need to
                    if (schedList != null) {
                        for (TimeoSingleSchedule currSched : schedList) {
                            View singleScheduleView = inflater.inflate(R.layout.view_single_schedule_label, null);

                            TextView lbl_schedule = (TextView) singleScheduleView.findViewById(R.id.lbl_schedule);
                            TextView lbl_schedule_direction = (TextView) singleScheduleView.findViewById(R.id
                                    .lbl_schedule_direction);

                            lbl_schedule.setText(TimeFormatter.formatTime(ActivityNewStop.this, currSched.getScheduleTime()));
                            lbl_schedule_direction.setText(" — " + currSched.getDirection());

                            mViewScheduleContainer.addView(singleScheduleView);
                        }

                        if (schedList.size() > 0) {
                            mViewScheduleContainer.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        }).execute();
    }

    /**
     * Fetches the bus lines from the API, and populates the line spinner when done.
     */
    public void getLinesFromAPI() {

        // fetch the directions
        (new AsyncTask<Void, Void, List<TimeoLine>>() {

            @Override
            protected void onPreExecute() {
                mSwipeRefreshLayout.setEnabled(true);
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected List<TimeoLine> doInBackground(Void... voids) {
                try {
                    return TimeoRequestHandler.getLines();
                } catch (Exception e) {
                    handleAsyncExceptions(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<TimeoLine> timeoLines) {
                if (timeoLines != null) {
                    mLineList.clear();
                    mLineList.addAll(timeoLines);

                    mFilteredLineList.clear();
                    mFilteredLineList.addAll(mLineList);

                    mSpinLine.setEnabled(true);
                    mSpinDirection.setEnabled(true);

                    for (int i = mFilteredLineList.size() - 1; i >= 0; i--) {
                        //if the last line in the list is the same line (but with a different direction)
                        if (i > 0 && mFilteredLineList.get(i).getId().equals(mFilteredLineList.get(i - 1).getDetails()
                                .getId())) {
                            mFilteredLineList.remove(i);
                        }
                    }

                    mLineAdapter.notifyDataSetChanged();
                }
            }

        }).execute();
    }

    /**
     * Displays an exception in a toast on the UI thread.
     *
     * @param e the exception to display
     */
    public void handleAsyncExceptions(final Exception e) {
        e.printStackTrace();

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (e instanceof TimeoBlockingMessageException) {
                    ((TimeoBlockingMessageException) e).getAlertMessage(ActivityNewStop.this).show();
                } else {
                    String message;

                    if (e instanceof TimeoException) {
                        if (e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
                            message = getString(R.string.error_toast_twisto_detailed,
                                    ((TimeoException) e).getErrorCode(), e.getMessage());
                        } else {
                            message = getString(R.string.error_toast_twisto, ((TimeoException) e).getErrorCode());
                        }
                    } else {
                        message = getString(R.string.loading_error);
                    }

                    mSwipeRefreshLayout.setEnabled(false);
                    mSwipeRefreshLayout.setRefreshing(false);

                    Snackbar.make(findViewById(R.id.content), message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.error_retry, new View.OnClickListener() {

	                            @Override
	                            public void onClick(View view) {
		                            getLinesFromAPI();
	                            }

                            })
                            .show();
                }
            }

        });
    }

    /**
     * Stores the selected bus stop in the database.
     */
    public void registerStopToDatabase() {
        TimeoStop stop = getCurrentStop();

        try {
            mDatabaseHandler.addStopToDatabase(stop);
            Toast.makeText(this, getResources().getString(R.string.added_toast, stop.toString()), Toast.LENGTH_SHORT).show();
            setResult(STOP_ADDED);
            finish();
        } catch (SQLiteConstraintException e) {
            // stop already in database
            Toast.makeText(this, getResources().getString(R.string.error_toast, getResources().getString(R.string
                    .add_error_duplicate)), Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e) {
            // one of the fields was null
            Toast.makeText(this, getResources().getString(R.string.error_toast, getResources().getString(R.string
                    .add_error_illegal_argument)), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Gets a list of directions for the selected bus line, as they're stored in the same object.
     *
     * @return a list of ID/name objects containing the id and name of the directions to display
     */
    private List<TimeoIDNameObject> getDirectionsList() {
        List<TimeoIDNameObject> directionsList = new ArrayList<>();

        for (TimeoLine line : mLineList) {
            if (line.getId().equals(getCurrentLine().getId())) {
                directionsList.add(line.getDirection());
            }
        }

        return directionsList;
    }

    /**
     * Gets the bus stop that's currently selected.
     *
     * @return a stop
     */
    public TimeoStop getCurrentStop() {
        return (TimeoStop) mSpinStop.getItemAtPosition(mSpinStop.getSelectedItemPosition());
    }

    /**
     * Gets the bus line direction that's currently selected.
     *
     * @return an ID/name object for the direction
     */
    public TimeoIDNameObject getCurrentDirection() {
        return (TimeoIDNameObject) mSpinDirection.getItemAtPosition(mSpinDirection.getSelectedItemPosition());
    }

    /**
     * Gets the bus line that's currently selected.
     *
     * @return a line
     */
    public TimeoLine getCurrentLine() {
        return (TimeoLine) mSpinLine.getItemAtPosition(mSpinLine.getSelectedItemPosition());
    }

}
