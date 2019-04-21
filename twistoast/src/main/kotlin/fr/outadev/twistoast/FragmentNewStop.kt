/*
 * Twistoast - FragmentNewStop.kt
 * Copyright (C) 2013-2018 Baptiste Candellier
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
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import fr.outadev.twistoast.extensions.brighten
import fr.outadev.twistoast.extensions.getAlertMessage
import fr.outadev.twistoast.extensions.toColor
import fr.outadev.twistoast.model.*
import kotlinx.android.synthetic.main.fragment_new_stop.*
import kotlinx.android.synthetic.main.view_schedule_row.*
import kotlinx.android.synthetic.main.view_single_schedule_label.view.*
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast


class FragmentNewStop : Fragment() {

    private lateinit var itemNext: MenuItem

    private lateinit var viewModel: NewStopViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this).get(NewStopViewModel::class.java)

        viewModel.load()

        viewModel.lines.observe(this, Observer { lines ->
            when (lines) {
                is Result.Success -> spinLine.adapter =
                        ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, lines.data)

                is Result.Failure -> displayError(lines.e)
            }
        })

        viewModel.directions.observe(this, Observer { directions ->
            when (directions) {
                is Result.Success -> spinDirection.adapter =
                        ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, directions.data)

                is Result.Failure -> displayError(directions.e)
            }
        })

        viewModel.stops.observe(this, Observer { stops ->
            when (stops) {
                is Result.Success -> spinLine.adapter =
                        ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, stops.data)

                is Result.Failure -> displayError(stops.e)
            }
        })

        viewModel.isLineListEnabled.observe(this, Observer {
            spinLine.isEnabled = it ?: false
        })

        viewModel.isDirectionListEnabled.observe(this, Observer {
            spinDirection.isEnabled = it ?: false
        })

        viewModel.isStopListEnabled.observe(this, Observer {
            spinStop.isEnabled = it ?: false
        })

        viewModel.isRefreshing.observe(this, Observer {
            swipeRefreshContainer.isEnabled = it ?: false
            swipeRefreshContainer.isRefreshing = it ?: false
        })

        viewModel.selectedLine.observe(this, Observer {
            // set loading labels
            rowLineId.text = ""
            rowDirectionName.text = resources.getString(R.string.loading_data)
            rowStopName.text = resources.getString(R.string.loading_data)

            viewScheduleContainer.removeAllViewsInLayout()
            viewScheduleContainer.visibility = View.GONE

            itemNext.isEnabled = false

            it?.let { line ->
                // set the line view
                rowLineId.text = line.id

                val lineDrawable = rowLineIdContainer.background as GradientDrawable
                val brighterColor = line.color.toColor()?.brighten()
                brighterColor?.let { color -> lineDrawable.setColor(color.toArgb()) }

                // adapt the size based on the size of the line ID
                when {
                    rowLineId.text.length > 3 -> rowLineId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    rowLineId.text.length > 2 -> rowLineId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
                    else -> rowLineId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                }
            }
        })

        viewModel.selectedDirection.observe(this, Observer {
            // set loading labels
            rowDirectionName.text = resources.getString(R.string.loading_data)
            viewScheduleContainer.removeAllViewsInLayout()
            viewScheduleContainer.visibility = View.GONE

            itemNext.isEnabled = false

            it?.let { direction ->
                val dir = direction.name ?: direction.id
                rowDirectionName.text = resources.getString(R.string.direction_name, dir)
            }
        })

        viewModel.selectedStop.observe(this, Observer { it ->
            rowStopName.text = resources.getString(R.string.loading_data)
            viewScheduleContainer.removeAllViewsInLayout()
            viewScheduleContainer.visibility = View.GONE

            itemNext.isEnabled = true

            it?.let { stop ->
                rowStopName.text = resources.getString(R.string.stop_name, stop.name)
            }
        })

        viewModel.schedule.observe(this, Observer {
            when (it) {
                is Result.Success -> {
                    it.data.let { schedule ->
                        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                        swipeRefreshContainer.isEnabled = false
                        swipeRefreshContainer.isRefreshing = false

                        val schedList = schedule.schedules

                        // set the schedule labels, if we need to
                        for (currSched in schedList) {
                            val singleScheduleView = inflater.inflate(R.layout.view_single_schedule_label, null)

                            singleScheduleView.lbl_schedule.text = context?.let { ctx -> TimeFormatter.formatTime(ctx, currSched.scheduleTime) }
                            singleScheduleView.lbl_schedule_direction.text = currSched.direction

                            if (!currSched.direction.isNullOrBlank())
                                singleScheduleView.lbl_schedule_separator.visibility = View.VISIBLE

                            viewScheduleContainer.addView(singleScheduleView)
                        }

                        if (schedList.isNotEmpty()) {
                            viewScheduleContainer.visibility = View.VISIBLE
                        }
                    }
                }

                is Result.Failure -> displayError(it.e)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_stop, menu)
        itemNext = menu.findItem(R.id.action_ok)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_stop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshContainer.setColorSchemeResources(
                R.color.twisto_primary, R.color.twisto_secondary,
                R.color.twisto_primary, R.color.twisto_secondary)

        spinLine.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parentView: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.selectedLine.value = spinLine.getItemAtPosition(position) as Line
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                viewModel.selectedLine.value = null
            }
        }

        spinDirection.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parentView: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.selectedDirection.value = spinDirection.getItemAtPosition(position) as Direction
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                viewModel.selectedDirection.value = null
            }
        }

        spinStop.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parentView: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.selectedStop.value = spinStop.getItemAtPosition(position) as Stop
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                viewModel.selectedStop.value = null
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_ok -> {
                // add the current stop to the database
                registerStopToDatabase()
                true
            }

            android.R.id.home -> {
                // go back
                findNavController().navigateUp()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Displays an exception in a toast on the UI thread.

     * @param e the exception to display
     */
    private fun displayError(e: Throwable) {
        e.printStackTrace()

        when (e) {
            is BlockingMessageException -> e.getAlertMessage(context).show()
            else -> {
                val message: String = if (e is DataProviderException) {
                    if (!e.message?.trim { it <= ' ' }!!.isEmpty()) {
                        getString(R.string.error_toast_twisto_detailed, e.errorCode, e.message)
                    } else {
                        getString(R.string.error_toast_twisto, e.errorCode)
                    }

                } else {
                    getString(R.string.loading_error)
                }

                viewModel.isRefreshing.value = false

                view?.let {
                    Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.error_retry) { viewModel.load() }
                            .show()
                }
            }
        }

    }

    /**
     * Stores the selected bus stop in the database.
     */
    private fun registerStopToDatabase() {
        try {
            viewModel.registerStopToDatabase()
            toast(getString(R.string.added_toast, viewModel.selectedStop.toString()))
            findNavController().navigateUp()
        } catch (e: SQLiteConstraintException) {
            // stop already in database
            longToast(getString(R.string.error_toast, getString(R.string.add_error_duplicate)))
        } catch (e: IllegalArgumentException) {
            // one of the fields was null
            longToast(getString(R.string.error_toast, getString(R.string.add_error_illegal_argument)))
        }
    }

}
