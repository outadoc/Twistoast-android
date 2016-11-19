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
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import fr.outadev.android.transport.timeo.TimeoStop
import fr.outadev.android.transport.timeo.TimeoStopSchedule
import fr.outadev.twistoast.uiutils.Colors

/**
 * An array adapter for the main list of bus stops.
 *
 * @author outadoc
 */
class RecyclerAdapterRealtime(val activity: Activity, private val stopsList: MutableList<TimeoStop>, private val schedules: MutableMap<TimeoStop, TimeoStopSchedule>) : RecyclerView.Adapter<StopScheduleViewHolder>(), IRecyclerAdapterAccess {

    private val database: Database
    private val config: ConfigurationManager

    var longPressedItemPosition: Int? = null

    init {
        database = Database(DatabaseOpenHelper())
        config = ConfigurationManager()
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

        val dir = if (currentStopId.line.direction.name != null) currentStopId.line.direction.name else currentStopId.line.direction.id
        view.rowDirectionName.text = view.rowDirectionName.context.getString(R.string.direction_name, dir)

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
                view.lblScheduleDirection[i]?.text = currSched.direction
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

        //view.container.setOnLongClickListener { v -> longClickListener.onLongClick(v, position) }
        view.imgStopWatched.visibility = if (currentStopId.isWatched) View.VISIBLE else View.GONE

        view.itemView.setOnLongClickListener {
            longPressedItemPosition = position
            false
        }
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

    fun getItem(position: Int): TimeoStop? {
        return stopsList[position]
    }

    companion object {
        val TAG: String = RecyclerAdapterRealtime::class.java.simpleName
        val NB_SCHEDULES_DISPLAYED = 2
    }

}
