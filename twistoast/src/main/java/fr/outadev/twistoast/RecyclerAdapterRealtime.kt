/*
 * Twistoast - RecyclerAdapterRealtime.kt
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

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import fr.outadev.android.transport.timeo.*
import fr.outadev.twistoast.background.BackgroundTasksManager
import fr.outadev.twistoast.uiutils.Colors
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * An array adapter for the main list of bus stops.
 *
 * @author outadoc
 */
class RecyclerAdapterRealtime(val activity: Activity, private val stopsList: MutableList<TimeoStop>, private val mStopsListContainer: IStopsListContainer, private val mParentView: View) : RecyclerView.Adapter<StopScheduleViewHolder>(), IRecyclerAdapterAccess {

    private val database: Database
    private val config: ConfigurationManager
    private val referenceUpdater: TimeoStopReferenceUpdater
    private val requestHandler: TimeoRequestHandler
    private val schedules: MutableMap<TimeoStop, TimeoStopSchedule>

    private var networkCount = 0

    init {
        database = Database(DatabaseOpenHelper())
        referenceUpdater = TimeoStopReferenceUpdater()
        config = ConfigurationManager()
        requestHandler = TimeoRequestHandler()

        schedules = mutableMapOf<TimeoStop, TimeoStopSchedule>()
        networkCount = database.networksCount
    }

    /**
     * Fetches every stop schedule from the API and reloads everything.
     */
    fun updateScheduleData() {
        // start refreshing schedules
        doAsync {
            try {
                // Get the schedules and put them in a list
                var schedulesList: List<TimeoStopSchedule>?
                var scheduleMap: Map<TimeoStop, TimeoStopSchedule>

                schedulesList = requestHandler.getMultipleSchedules(stopsList)
                scheduleMap = schedulesList.associateBy({ it.stop }, { it })

                val outdated = requestHandler.checkForOutdatedStops(stopsList, schedulesList)

                // If there are outdated reference numbers, update those stops
                if (outdated > 0) {
                    Log.e(TAG, "Found $outdated stops, trying to update references")
                    referenceUpdater.updateAllStopReferences(stopsList)

                    // Reload with the updated stops
                    schedulesList = requestHandler.getMultipleSchedules(stopsList)
                    scheduleMap = schedulesList.associateBy({ it.stop }, { it })
                }

                uiThread {
                    schedules.clear()
                    schedules.putAll(scheduleMap)

                    networkCount = database.networksCount

                    notifyDataSetChanged()
                    mStopsListContainer.endRefresh(scheduleMap.isNotEmpty())

                    if (outdated > 0) {
                        mStopsListContainer.onUpdatedStopReferences()
                    }
                }

            } catch (e: TimeoBlockingMessageException) {
                e.printStackTrace()
                uiThread {
                    // It's it's a blocking message, display it in a dialog
                    e.getAlertMessage(activity).show()
                }

            } catch (e: TimeoException) {
                e.printStackTrace()
                uiThread {
                    val message: String

                    // If there are details to the error, display them. Otherwise, only display the error code
                    if (!e.message?.trim { it <= ' ' }!!.isEmpty()) {
                        message = activity.getString(R.string.error_toast_twisto_detailed, e.errorCode, e.message)
                    } else {
                        message = activity.getString(R.string.error_toast_twisto, e.errorCode)
                    }

                    Snackbar.make(mParentView, message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.error_retry) { updateScheduleData() }.show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                uiThread {
                    mStopsListContainer.endRefresh(false)
                    Snackbar.make(mParentView, R.string.loading_error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.error_retry) { updateScheduleData() }.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): StopScheduleViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.view_schedule_row, parent, false)
        return StopScheduleViewHolder(v)
    }

    override fun onBindViewHolder(view: StopScheduleViewHolder, position: Int) {
        // Get the stop we're inflating
        val currentStopId = stopsList[position]

        view.lineDrawable.setColor(Colors.getBrighterColor(Color.parseColor(currentStopId.line.color)))

        view.rowLineId.text = currentStopId.line.id
        view.rowStopName.text = view.rowStopName.context.getString(R.string.stop_name, currentStopId.name)
        view.rowDirectionName.text = view.rowDirectionName.context.getString(R.string.direction_name, currentStopId.line.direction.name)

        view.resetView()

        // Add the new schedules one by one
        if (schedules.containsKey(currentStopId) && schedules[currentStopId] != null) {
            val currentStop: TimeoStopSchedule = schedules[currentStopId]!!

            // Get the schedules for this stop
            currentStop.schedules.forEachIndexed {
                i, currSched ->
                // We don't update from database all the time, so we can't figure this out by just updating everything.
                // If there is a bus coming, tell the stop that it's not watched anymore.
                // This won't work all the time, but it's not too bad.
                if (currSched.scheduleTime.isBeforeNow) {
                    currentStopId.isWatched = false
                }

                view.lblScheduleTime[i]?.text = TimeFormatter.formatTime(view.lblScheduleTime[i]!!.context, currSched.scheduleTime)
                view.lblScheduleDirection[i]?.text = " — " + currSched.direction
            }

            if (currentStop.schedules.isEmpty()) {
                // If no schedules are available, add a fake one to inform the user
                view.lblScheduleTime[0]?.setText(R.string.no_upcoming_stops)
            }

            if (currentStop.trafficMessages.isNotEmpty()) {
                val message = currentStop.trafficMessages.first()
                view.lblStopTrafficTitle.text = message.title
                view.lblStopTrafficMessage.text = message.body

                view.viewStopTrafficInfoContainer.visibility = View.VISIBLE
            }

            // Fade in the row!
            if (view.container.alpha != 1.0f) {
                view.container.alpha = 1.0f

                val alphaAnim = AlphaAnimation(0.4f, 1.0f)
                alphaAnim.duration = 500
                view.container.startAnimation(alphaAnim)
            }

        } else {
            // If we can't find the schedules we asked for in the hashmap, something went wrong. :c
            // It should be noted that it normally happens the first time the list is loaded, since no data was downloaded yet.
            Log.e(TAG, "missing stop schedule for $currentStopId (ref=${currentStopId.reference}); ref outdated?")

            // Make the row look a bit translucent to make it stand out
            view.lblScheduleTime[0]?.setText(R.string.no_upcoming_stops)
            view.container.alpha = 0.4f
        }

        view.container.setOnLongClickListener { v -> longClickListener.onLongClick(v, position) }
        view.imgStopWatched.visibility = if (currentStopId.isWatched) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return stopsList.size
    }

    override fun shouldItemHaveSeparator(position: Int): Boolean {
        // If it's the last item, no separator
        if (position < 0 || position == stopsList.size - 1) {
            return false
        }

        val item = stopsList[position]
        val nextItem = stopsList[position + 1]

        val criteria = config.listSortOrder

        if (criteria == Database.SortBy.STOP) {
            // If the next item's stop is the same as this one, don't draw a separator either
            return !(item.id == nextItem.id || item.name == nextItem.name)
        } else {
            // If the next item's line is the same as this one, don't draw a separator either
            return item.line.id != nextItem.line.id
        }
    }

    private val longClickListener = object : StopScheduleViewHolder.IOnLongClickListener {

        override fun onLongClick(view: View, position: Int): Boolean {
            if (!mStopsListContainer.isRefreshing) {
                val currentStop = stopsList[position]

                // Menu items
                val contextualMenuItems = arrayOf(
                        view.context.getString(R.string.stop_action_delete),
                        view.context.getString(if (!currentStop.isWatched) R.string.stop_action_watch else R.string.stop_action_unwatch))

                // Build the long click contextual menu
                val builder = AlertDialog.Builder(activity)
                builder.setItems(contextualMenuItems) { dialog, which ->
                    if (which == 0) {
                        deleteStopAction(currentStop, which)
                    } else if (which == 1 && !currentStop.isWatched) {
                        startWatchingStopAction(currentStop)
                    } else if (which == 1) {
                        stopWatchingStopAction(currentStop)
                    }
                }

                builder.show()
            }

            return true
        }

        private fun deleteStopAction(stop: TimeoStop, position: Int) {
            // Remove from the database and the interface
            database.deleteStop(stop)
            stopsList.remove(stop)

            if (stop.isWatched) {
                database.stopWatchingStop(stop)
                stop.isWatched = false
            }

            if (stopsList.isEmpty()) {
                mStopsListContainer.setNoContentViewVisible(true)
            }

            notifyDataSetChanged()

            Snackbar.make(mParentView, R.string.confirm_delete_success, Snackbar.LENGTH_LONG).setAction(R.string.cancel_stop_deletion) {
                Log.i(TAG, "restoring stop " + stop)

                database.addStopToDatabase(stop)
                stopsList.add(position, stop)
                notifyDataSetChanged()
            }.show()
        }

        private fun startWatchingStopAction(stop: TimeoStop) {
            // We wish to get notifications about this upcoming stop
            database.addToWatchedStops(stop)
            stop.isWatched = true
            notifyDataSetChanged()

            // Turn the notifications on
            BackgroundTasksManager.enableStopAlarmJob(activity.applicationContext)

            Snackbar.make(mParentView, mParentView.context.getString(R.string.notifs_enable_toast, stop.name), Snackbar.LENGTH_LONG).setAction(R.string.cancel_stop_deletion) {
                database.stopWatchingStop(stop)
                stop.isWatched = false
                notifyDataSetChanged()

                // Turn the notifications back off if necessary
                if (database.watchedStopsCount == 0) {
                    BackgroundTasksManager.disableStopAlarmJob(activity.applicationContext)
                }
            }.show()
        }

        private fun stopWatchingStopAction(stop: TimeoStop) {
            // JUST STOP THESE NOTIFICATIONS ALREADY GHGHGHBLBLBL
            database.stopWatchingStop(stop)
            stop.isWatched = false
            notifyDataSetChanged()

            // Turn the notifications back off if necessary
            if (database.watchedStopsCount == 0) {
                BackgroundTasksManager.disableStopAlarmJob(activity.applicationContext)
            }

            val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(Integer.valueOf(stop.id)!!)

            Snackbar.make(mParentView, mParentView.context.getString(R.string.notifs_disable_toast, stop.name), Snackbar.LENGTH_LONG).show()
        }

    }

    companion object {
        val TAG: String = RecyclerAdapterRealtime::class.java.simpleName
        val NB_SCHEDULES_DISPLAYED = 2
    }

}
