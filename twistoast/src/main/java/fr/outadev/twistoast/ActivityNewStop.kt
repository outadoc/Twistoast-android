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

package fr.outadev.twistoast

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import fr.outadev.android.transport.timeo.*
import kotlinx.android.synthetic.main.activity_new_stop.*
import kotlinx.android.synthetic.main.view_schedule_row.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

/**
 * Activity that allows the user to add a bus stop to the app.

 * @author outadoc
 */
class ActivityNewStop : ThemedActivity() {

    private var lineList = mutableListOf<TimeoLine>()
    private var directionList = mutableListOf<TimeoIDNameObject>()
    private var stopList = mutableListOf<TimeoStop>()

    private var filteredLineList = mutableListOf<TimeoLine>()

    private var lineAdapter: ArrayAdapter<TimeoLine>? = null
    private var directionAdapter: ArrayAdapter<TimeoIDNameObject>? = null
    private var stopAdapter: ArrayAdapter<TimeoStop>? = null

    private var viewScheduleContainer: LinearLayout? = null

    private var itemNext: MenuItem? = null

    private var databaseHandler: Database? = null
    private var requestHandler = TimeoRequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup everything
        setContentView(R.layout.activity_new_stop)
        setResult(NO_STOP_ADDED)

        val toolbar = findViewById(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close_white)

        databaseHandler = Database(DatabaseOpenHelper.getInstance(this))

        viewScheduleContainer = findViewById(R.id.view_schedule_labels_container) as LinearLayout

        swipeRefreshContainer.setColorSchemeResources(
                R.color.twisto_primary, R.color.twisto_secondary,
                R.color.twisto_primary, R.color.twisto_secondary)
        swipeRefreshContainer.isRefreshing = true

        spinLine.isEnabled = false
        spinDirection.isEnabled = false
        spinStop.isEnabled = false

        //setup spinners here
        setupLineSpinner()
        setupDirectionSpinner()
        setupStopSpinner()

        getLinesFromAPI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.add_stop, menu)
        itemNext = menu.findItem(R.id.action_ok)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_ok -> {
                // add the current stop to the database
                registerStopToDatabase()
                return true
            }
            android.R.id.home -> {
                // go back
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Initialises, sets up the even listeners, and populates the line spinner.
     */
    fun setupLineSpinner() {
        lineAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filteredLineList)
        lineAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinLine!!.adapter = lineAdapter

        // when a line has been selected
        spinLine!!.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parentView: AdapterView<*>, view: View, position: Int, id: Long) {
                // set loading labels
                rowLineId.text = ""
                rowDirectionName.text = resources.getString(R.string.loading_data)
                rowStopName.text = resources.getString(R.string.loading_data)

                viewScheduleContainer!!.removeAllViewsInLayout()
                viewScheduleContainer!!.visibility = View.GONE

                spinStop!!.isEnabled = false

                if (itemNext != null) {
                    itemNext!!.isEnabled = false
                }

                // get the selected line
                val item = currentLine

                if (item != null && item.id != null) {
                    // set the line view
                    rowLineId.text = item.id

                    val lineDrawable = rowLineIdContainer.background as GradientDrawable
                    lineDrawable.setColor(Colors.getBrighterColor(Color.parseColor(item.color)))

                    spinDirection!!.isEnabled = true

                    // adapt the size based on the size of the line ID
                    if (rowLineId.text.length > 3) {
                        rowLineId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    } else if (rowLineId.text.length > 2) {
                        rowLineId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
                    } else {
                        rowLineId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                    }

                    directionList.clear()
                    directionList.addAll(getDirectionsList())
                    setupDirectionSpinner()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }

        }
    }

    /**
     * Initialises, sets up the even listeners, and populates the direction spinner.
     */
    fun setupDirectionSpinner() {
        directionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, directionList)
        directionAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinDirection!!.adapter = directionAdapter

        // when a line has been selected
        spinDirection!!.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parentView: AdapterView<*>, view: View, position: Int, id: Long) {
                // set loading labels
                rowDirectionName.text = resources.getString(R.string.loading_data)
                viewScheduleContainer!!.removeAllViewsInLayout()
                viewScheduleContainer!!.visibility = View.GONE

                itemNext!!.isEnabled = false
                spinStop!!.isEnabled = false

                if (currentLine != null && currentDirection != null && currentLine!!.id != null
                        && currentDirection!!.id != null) {
                    rowDirectionName.text = resources.getString(R.string.direction_name, currentDirection!!.name)

                    object : AsyncTask<Void, Void, List<TimeoStop>>() {

                        override fun doInBackground(vararg voids: Void): List<TimeoStop>? {
                            try {
                                currentLine!!.direction = currentDirection
                                return requestHandler.getStops(currentLine)
                            } catch (e: Exception) {
                                handleAsyncExceptions(e)
                            }

                            return null
                        }

                        override fun onPreExecute() {
                            swipeRefreshContainer.isEnabled = true
                            swipeRefreshContainer.isRefreshing = true
                        }


                        override fun onPostExecute(timeoStops: List<TimeoStop>?) {
                            if (timeoStops != null) {
                                spinStop!!.isEnabled = true

                                stopList.clear()
                                stopList.addAll(timeoStops)
                                setupStopSpinner()
                            }
                        }

                    }.execute()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }

        }
    }

    /**
     * Initialises, sets up the even listeners, and populates the stop spinner.
     */
    fun setupStopSpinner() {
        stopAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stopList)
        stopAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinStop!!.adapter = stopAdapter

        // when a stop has been selected
        spinStop!!.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parentView: AdapterView<*>, view: View, position: Int, id: Long) {
                rowStopName.text = resources.getString(R.string.loading_data)
                viewScheduleContainer!!.removeAllViewsInLayout()
                viewScheduleContainer!!.visibility = View.GONE

                val stop = currentStop
                itemNext!!.isEnabled = true

                if (stop != null && stop.id != null) {
                    rowStopName.text = resources.getString(R.string.stop_name, stop.name)
                    updateSchedulePreview()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }

        }
    }

    fun updateSchedulePreview() {
        object : AsyncTask<Void, Void, TimeoStopSchedule>() {

            override fun onPreExecute() {
                swipeRefreshContainer.isEnabled = true
                swipeRefreshContainer.isRefreshing = true
            }

            override fun doInBackground(vararg voids: Void): TimeoStopSchedule? {
                try {
                    return requestHandler.getSingleSchedule(currentStop)
                } catch (e: Exception) {
                    handleAsyncExceptions(e)
                }

                return null
            }

            override fun onPostExecute(schedule: TimeoStopSchedule?) {
                val inflater = this@ActivityNewStop.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                swipeRefreshContainer.isEnabled = false
                swipeRefreshContainer.isRefreshing = false

                if (schedule != null) {
                    val schedList = schedule.schedules

                    // set the schedule labels, if we need to
                    if (schedList != null) {
                        for (currSched in schedList) {
                            val singleScheduleView = inflater.inflate(R.layout.view_single_schedule_label, null)

                            val lbl_schedule = singleScheduleView.findViewById(R.id.lbl_schedule) as TextView
                            val lbl_schedule_direction = singleScheduleView.findViewById(R.id.lbl_schedule_direction) as TextView

                            lbl_schedule.text = TimeFormatter.formatTime(this@ActivityNewStop, currSched.scheduleTime)
                            lbl_schedule_direction.text = " — " + currSched.direction

                            viewScheduleContainer!!.addView(singleScheduleView)
                        }

                        if (schedList.size > 0) {
                            viewScheduleContainer!!.visibility = View.VISIBLE
                        }
                    }
                }
            }

        }.execute()
    }

    /**
     * Fetches the bus lines from the API, and populates the line spinner when done.
     */
    fun getLinesFromAPI() {

        // fetch the directions
        object : AsyncTask<Void, Void, List<TimeoLine>>() {

            override fun onPreExecute() {
                swipeRefreshContainer.isEnabled = true
                swipeRefreshContainer.isRefreshing = true
            }

            override fun doInBackground(vararg voids: Void): List<TimeoLine>? {
                try {
                    return requestHandler.lines
                } catch (e: Exception) {
                    handleAsyncExceptions(e)
                }

                return null
            }

            override fun onPostExecute(timeoLines: List<TimeoLine>?) {
                if (timeoLines != null) {
                    lineList.clear()
                    lineList.addAll(timeoLines)

                    filteredLineList.clear()
                    filteredLineList.addAll(lineList)

                    spinLine!!.isEnabled = true
                    spinDirection!!.isEnabled = true

                    for (i in filteredLineList.indices.reversed()) {
                        //if the last line in the list is the same line (but with a different direction)
                        if (i > 0 && filteredLineList[i].id == filteredLineList[i - 1].details.id) {
                            filteredLineList.removeAt(i)
                        }
                    }

                    lineAdapter!!.notifyDataSetChanged()
                }
            }

        }.execute()
    }

    /**
     * Displays an exception in a toast on the UI thread.

     * @param e the exception to display
     */
    fun handleAsyncExceptions(e: Exception) {
        e.printStackTrace()

        runOnUiThread {
            if (e is TimeoBlockingMessageException) {
                e.getAlertMessage(this@ActivityNewStop).show()
            } else {
                val message: String

                if (e is TimeoException) {
                    if (!e.message?.trim { it <= ' ' }!!.isEmpty()) {
                        message = getString(R.string.error_toast_twisto_detailed, e.errorCode, e.message)
                    } else {
                        message = getString(R.string.error_toast_twisto, e.errorCode)
                    }
                } else {
                    message = getString(R.string.loading_error)
                }

                swipeRefreshContainer.isEnabled = false
                swipeRefreshContainer.isRefreshing = false

                Snackbar.make(findViewById(R.id.content), message, Snackbar.LENGTH_LONG).setAction(R.string.error_retry) { getLinesFromAPI() }.show()
            }
        }
    }

    /**
     * Stores the selected bus stop in the database.
     */
    fun registerStopToDatabase() {
        val stop = currentStop

        try {
            databaseHandler!!.addStopToDatabase(stop)
            toast(resources.getString(R.string.added_toast, stop!!.toString()))
            setResult(STOP_ADDED)
            finish()
        } catch (e: SQLiteConstraintException) {
            // stop already in database
            longToast(resources.getString(R.string.error_toast, resources.getString(R.string.add_error_duplicate)))
        } catch (e: IllegalArgumentException) {
            // one of the fields was null
            longToast(resources.getString(R.string.error_toast, resources.getString(R.string.add_error_illegal_argument)))
        }

    }

    /**
     * Gets a list of directions for the selected bus line, as they're stored in the same object.

     * @return a list of ID/name objects containing the id and name of the directions to display
     */
    fun getDirectionsList() : List<TimeoIDNameObject> {
        val directionsList = ArrayList<TimeoIDNameObject>()

        for (line in lineList) {
            if (line.id == currentLine!!.id) {
                directionsList.add(line.direction)
            }
        }

        return directionsList
    }

    /**
     * Gets the bus stop that's currently selected.

     * @return a stop
     */
    val currentStop: TimeoStop?
        get() = spinStop!!.getItemAtPosition(spinStop!!.selectedItemPosition) as TimeoStop

    /**
     * Gets the bus line direction that's currently selected.

     * @return an ID/name object for the direction
     */
    val currentDirection: TimeoIDNameObject?
        get() = spinDirection!!.getItemAtPosition(spinDirection!!.selectedItemPosition) as TimeoIDNameObject

    /**
     * Gets the bus line that's currently selected.

     * @return a line
     */
    val currentLine: TimeoLine?
        get() = spinLine!!.getItemAtPosition(spinLine!!.selectedItemPosition) as TimeoLine

    companion object {
        const val NO_STOP_ADDED = 0
        const val STOP_ADDED = 1
    }

}
