/*
 * Twistoast - NewStopViewModel.kt
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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import fr.outadev.twistoast.model.*
import fr.outadev.twistoast.model.Result.Companion.failure
import fr.outadev.twistoast.model.Result.Companion.loading
import fr.outadev.twistoast.model.Result.Companion.success
import fr.outadev.twistoast.persistence.StopRepository
import fr.outadev.twistoast.providers.BusDataRepository

class NewStopViewModel : ViewModel() {

    private val repository = StopRepository()
    private val api = BusDataRepository()

    lateinit var lines: LiveData<Result<List<Line>>>

    val selectedLine = MutableLiveData<Line>()
    val selectedDirection = MutableLiveData<Direction>()
    val selectedStop = MutableLiveData<Stop>()

    val directions: LiveData<Result<List<Direction>>> = map(selectedLine) { selectedLine ->
        selectedLine?.let {
            lines.value?.let { lines ->
                when (lines) {
                    is Result.Success -> {
                        isRefreshing.value = false
                        success(lines.data
                                .filter { line -> line.id == selectedLine.id }
                                .map(Line::direction))
                    }

                    is Result.Failure -> {
                        isRefreshing.value = false
                        failure(lines.e)
                    }

                    is Result.Loading -> {
                        isRefreshing.value = true
                        loading(lines.loading)
                    }
                }
            }
        }
    }

    val stops: LiveData<Result<List<Stop>>> = switchMap(selectedDirection) { direction ->
        direction?.let {
            selectedLine.value?.let { line ->
                line.direction = it
                api.getStops(line)
            }
        }
    }

    val schedule: LiveData<Result<StopSchedule>> = switchMap(selectedStop) { stop ->
        api.getSingleSchedule(stop)
    }

    val isLineListEnabled: LiveData<Boolean>
        get() = map(lines) { lines -> lines != null }

    val isDirectionListEnabled: LiveData<Boolean> =
            map(directions) { directions -> directions != null }

    val isStopListEnabled: LiveData<Boolean> =
            map(stops) { stops -> stops != null }

    val isRefreshing = MutableLiveData<Boolean>()

    fun load() {
        lines = api.getLines()
    }

    fun registerStopToDatabase() {
        repository.addStopToDatabase(selectedStop.value)
    }
}