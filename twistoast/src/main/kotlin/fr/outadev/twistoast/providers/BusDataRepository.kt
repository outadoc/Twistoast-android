/*
 * Twistoast - BusDataRepository.kt
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

/*
 * Twistoast - BusDataRepository.ktright (C) 2013-2018 Baptiste Candellier
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

package fr.outadev.twistoast.providers

import fr.outadev.android.transport.KeolisDao
import fr.outadev.twistoast.model.Line
import fr.outadev.twistoast.model.Stop
import fr.outadev.twistoast.model.StopSchedule

class BusDataRepository : IBusDataRepository {

    private val api = KeolisDao()

    override fun getStops(line: Line) =
            api.getStops(line)

    override fun getSingleSchedule(stop: Stop) =
            api.getSingleSchedule(stop)

    override fun getMultipleSchedules(stops: List<Stop>) =
            api.getMultipleSchedules(KeolisDao.DEFAULT_NETWORK_CODE, stops)

    override fun getLines(networkCode: Int) =
            api.getLines(KeolisDao.DEFAULT_NETWORK_CODE)

    override fun getStops(networkCode: Int, line: Line) =
            api.getStops(networkCode, line)

    override fun getStopsByCode(networkCode: Int, codes: List<Int>) =
            api.getStopsByCode(networkCode, codes)

    override fun getSingleSchedule(networkCode: Int, stop: Stop) =
            api.getSingleSchedule(networkCode, stop)

    override fun getMultipleSchedules(networkCode: Int, stops: List<Stop>) =
            api.getMultipleSchedules(networkCode, stops)

    override fun checkForOutdatedStops(stops: List<Stop>, schedules: List<StopSchedule>) =
            api.checkForOutdatedStops(stops, schedules)
}