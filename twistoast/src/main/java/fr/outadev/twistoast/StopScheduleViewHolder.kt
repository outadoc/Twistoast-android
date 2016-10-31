/*
 * Twistoast - StopScheduleViewHolder.kt
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

import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import fr.outadev.twistoast.uiutils.collapse
import fr.outadev.twistoast.uiutils.expand
import org.jetbrains.anko.onClick

/**
 * Container for an item in the list. Here, this corresponds to a bus stop, and all the info
 * displayed for it (schedules, and metadata).
 */
class StopScheduleViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    val container: LinearLayout
    val rowLineIdContainer: FrameLayout

    val rowLineId: TextView
    val rowStopName: TextView
    val rowDirectionName: TextView
    val viewScheduleContainer: LinearLayout
    val imgStopWatched: ImageView
    val lineDrawable: GradientDrawable

    val lblStopTrafficTitle: TextView
    val lblStopTrafficMessage: TextView

    val lblScheduleTime = arrayOfNulls<TextView>(RecyclerAdapterRealtime.NB_SCHEDULES_DISPLAYED)
    val lblScheduleDirection = arrayOfNulls<TextView>(RecyclerAdapterRealtime.NB_SCHEDULES_DISPLAYED)

    val viewStopTrafficInfoContainer: View

    var isExpanded: Boolean

    init {
        val inflater = LayoutInflater.from(v.context)
        container = v as LinearLayout

        isExpanded = false

        // Get references to the views
        rowLineIdContainer = v.findViewById(R.id.rowLineIdContainer) as FrameLayout

        rowLineId = v.findViewById(R.id.rowLineId) as TextView
        rowStopName = v.findViewById(R.id.rowStopName) as TextView
        rowDirectionName = v.findViewById(R.id.rowDirectionName) as TextView

        viewScheduleContainer = v.findViewById(R.id.viewScheduleContainer) as LinearLayout
        imgStopWatched = v.findViewById(R.id.imgStopWatched) as ImageView
        lineDrawable = rowLineIdContainer.background as GradientDrawable

        lblStopTrafficTitle = v.findViewById(R.id.lblStopTrafficTitle) as TextView
        lblStopTrafficMessage = v.findViewById(R.id.lblStopTrafficMessage) as TextView

        viewStopTrafficInfoContainer = v.findViewById(R.id.viewStopTrafficInfoContainer)

        // Stop traffic info is collapsed by default.
        // When it's clicked, we display the message.
        viewStopTrafficInfoContainer.onClick {
            if (!isExpanded)
                lblStopTrafficMessage.expand()
            else
                lblStopTrafficMessage.collapse()

            isExpanded = !isExpanded
            viewStopTrafficInfoContainer.requestLayout()
        }

        for (i in 0..RecyclerAdapterRealtime.NB_SCHEDULES_DISPLAYED - 1) {
            // Display the current schedule
            val singleScheduleView = inflater.inflate(R.layout.view_single_schedule_label, null)

            lblScheduleTime[i] = singleScheduleView.findViewById(R.id.lbl_schedule) as TextView
            lblScheduleDirection[i] = singleScheduleView.findViewById(R.id.lbl_schedule_direction) as TextView

            viewScheduleContainer.addView(singleScheduleView)
        }
    }

    fun resetView() {
        // Clear any previous data
        for (i in 0..RecyclerAdapterRealtime.NB_SCHEDULES_DISPLAYED - 1) {
            lblScheduleTime[i]?.text = ""
            lblScheduleDirection[i]?.text = ""
        }

        viewStopTrafficInfoContainer.visibility = View.GONE
        lblStopTrafficMessage.layoutParams.height = 0
        isExpanded = false
    }

}
