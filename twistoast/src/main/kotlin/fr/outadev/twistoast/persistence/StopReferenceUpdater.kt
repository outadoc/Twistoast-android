/*
 * Twistoast - StopReferenceUpdater.kt
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

package fr.outadev.twistoast.persistence

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.util.Log
import fr.outadev.twistoast.model.Result
import fr.outadev.twistoast.model.Result.Companion.failure
import fr.outadev.twistoast.model.Result.Companion.success
import fr.outadev.twistoast.model.Stop
import fr.outadev.twistoast.providers.BusDataRepository

/**
 * Fetches and updates all the references of the stops saved in our database.
 * Useful since they periodically change, and this class should allow the user
 * to update his list of stops without having to delete/re-add them.
 */
class StopReferenceUpdater(private val lifecycleOwner: LifecycleOwner) {

    private val database: IStopRepository = StopRepository()
    private val requestHandler = BusDataRepository()

    /**
     * Updates all the references of the bus stops in the database.
     * Read the classe's Javadoc for some more context.
     */
    fun updateAllStopReferences(stops: List<Stop>): LiveData<Result<Int>> {
        //update the progress
        Log.i(TAG, "updating stop references for ${stops.size} stops")

        //get the stops for the current line
        val result = MutableLiveData<Result<Int>>()
        requestHandler
                .getStopsByCode(codes = stops.map(Stop::id))
                .observe(lifecycleOwner, Observer { res ->
                    when (res) {
                        is Result.Success -> {
                            //update all the stops we received.
                            //obviously, only the ones existing in the database will be updated.
                            val nbUpdated = res.data.fold(0) { _, stop ->
                                database.updateStopReference(stop)
                            }

                            Log.i(TAG, "$nbUpdated stop references updated out of ${stops.size}")

                            result.value = success(nbUpdated)
                        }
                        is Result.Failure -> result.value = failure(res.e)
                    }
                })

        return result
    }

    companion object {
        private val TAG = StopReferenceUpdater::class.java.simpleName
    }

}
