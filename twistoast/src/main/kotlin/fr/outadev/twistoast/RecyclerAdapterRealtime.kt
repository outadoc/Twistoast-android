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

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.outadev.twistoast.model.TimeoStop
import fr.outadev.twistoast.model.TimeoStopSchedule

/**
 * An array adapter for the main list of bus stops.
 *
 * @author outadoc
 */
class RecyclerAdapterRealtime(private val stopsList: MutableList<TimeoStop>, private val schedules: MutableMap<TimeoStop, TimeoStopSchedule>) : RecyclerView.Adapter<StopScheduleViewHolder>(), IRecyclerAdapterAccess {

    private val config: ConfigurationManager = ConfigurationManager()

    var longPressedItemPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): StopScheduleViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.view_schedule_row, parent, false)
        return StopScheduleViewHolder(v)
    }

    override fun onBindViewHolder(view: StopScheduleViewHolder, position: Int) {
        // Get the stop we're inflating
        val stop = stopsList[position]

        view.displayStopInfo(stop)
        view.resetView()

        // Add the new schedules one by one
        if (schedules.containsKey(stop) && schedules[stop] != null) {
            val stopSchedule: TimeoStopSchedule = schedules[stop]!!

            stopSchedule.schedules.forEach {
                schedule ->
                // We don't update from database all the time, so we can't figure this out by just updating everything.
                // If there is a bus coming, tell the stop that it's not watched anymore.
                // This won't work all the time, but it's not too bad.
                if (schedule.scheduleTime.isBeforeNow) {
                    stop.isWatched = false
                }
            }

            view.displaySchedule(stopSchedule)

        } else {
            // If we can't find the schedules we asked for in the hashmap, something went wrong. :c
            // It should be noted that it normally happens the first time the list is loaded, since no data was downloaded yet.
            Log.e(TAG, "missing stop schedule for $stop (ref=${stop.reference}); ref outdated?")
            view.displayErrorSchedule(R.string.no_upcoming_stops, true)
        }

        view.imgStopWatched.visibility = if (stop.isWatched) View.VISIBLE else View.GONE

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

        if (criteria == SortBy.STOP) {
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
