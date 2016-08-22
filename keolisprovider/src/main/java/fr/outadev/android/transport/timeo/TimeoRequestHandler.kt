/*
 * Twistoast - TimeoRequestHandler.kt
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

package fr.outadev.android.transport.timeo

import android.util.Log
import fr.outadev.android.transport.HttpRequester
import fr.outadev.android.transport.IHttpRequester
import fr.outadev.android.transport.getNextDateForTime
import fr.outadev.android.transport.smartCapitalize
import fr.outadev.android.transport.timeo.dto.ErreurDTO
import fr.outadev.android.transport.timeo.dto.ListeHorairesDTO
import fr.outadev.android.transport.timeo.dto.ListeLignesDTO
import org.apache.commons.lang3.StringUtils.leftPad
import org.json.JSONObject
import org.json.JSONTokener
import org.simpleframework.xml.core.Persister
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URLEncoder
import java.util.*

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
class TimeoRequestHandler (val http: IHttpRequester = HttpRequester()) {

    private val serializer: Persister = Persister()

    /**
     * Fetches the bus stops for the specified line.
     */
    @Throws(IOException::class, TimeoException::class)
    fun getStops(line: TimeoLine): List<TimeoStop> {
        return getStops(line.networkCode, line)
    }

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    @Throws(TimeoException::class, IOException::class, XmlPullParserException::class)
    fun getSingleSchedule(stop: TimeoStop): TimeoStopSchedule {
        return getSingleSchedule(stop.line.networkCode, stop)
    }

    /**
     * Fetches the next bus schedules for the specified list of bus stops.
     */
    @Throws(TimeoException::class, IOException::class)
    fun getMultipleSchedules(stops: List<TimeoStop>): List<TimeoStopSchedule> {
        //if we don't specify any network code when calling getMultipleSchedules, we'll have to figure them out ourselves.
        //we can only fetch a list of schedules that are all part of the same network.
        //therefore, we'll have to separate them in different lists and request them individually.

        //the final list that will contain all of our schedules
        val finalScheduleList = ArrayList<TimeoStopSchedule>()
        val stopsByNetwork = stops.groupBy { it.line.networkCode }

        Log.i(TAG, "${stopsByNetwork.count()} different bus networks to refresh")

        stopsByNetwork.forEach {
            //request the schedules and add them to the final list
            finalScheduleList.addAll(getMultipleSchedules(it.key, it.value))
        }

        return finalScheduleList
    }


    @Throws(IOException::class, TimeoException::class)
    fun getLines(networkCode: Int = DEFAULT_NETWORK_CODE): List<TimeoLine> {
        val params = "xml=1"

        val result = http.requestWebPage(getEndpointUrl(networkCode), params, true)
        val res: ListeLignesDTO = serializer.read(ListeLignesDTO::class.java, result) ?: throw TimeoException("Service returned invalid data")

        checkForErrors(res.erreur)

        val linesList = res.alss.map {
                    als ->
                    TimeoLine(
                        id = als.ligne.code,
                        name = als.ligne.nom.smartCapitalize(),
                        direction = TimeoDirection(als.ligne.sens, als.ligne.vers.smartCapitalize()),
                        color = "#" + leftPad(Integer.toHexString(Integer.valueOf(als.ligne.couleur)), 6, '0'),
                        networkCode = networkCode)
                }

        return linesList
    }

    @Throws(IOException::class, TimeoException::class)
    fun getStops(networkCode: Int, line: TimeoLine): List<TimeoStop> {
        val params = "xml=1&ligne=${line.id}&sens=${line.direction.id}"

        val result = http.requestWebPage(getEndpointUrl(networkCode), params, true)
        val res: ListeLignesDTO = serializer.read(ListeLignesDTO::class.java, result) ?: throw TimeoException("Service returned invalid data")

        checkForErrors(res.erreur)

        val stopsList = res.alss.filter { it.arret.code != null && it.arret.nom != null }.map {
            als ->
            TimeoStop(
                    id = als.arret.code!!.toInt(),
                    name = als.arret.nom!!.smartCapitalize(),
                    reference = als.refs,
                    line = TimeoLine(
                            id = als.ligne.code,
                            name = als.ligne.nom.smartCapitalize(),
                            direction = TimeoDirection(als.ligne.sens, als.ligne.vers.smartCapitalize()),
                            color = "#" + leftPad(Integer.toHexString(Integer.valueOf(als.ligne.couleur)), 6, '0'),
                            networkCode = networkCode))
        }

        return stopsList
    }

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    @Throws(TimeoException::class, IOException::class, XmlPullParserException::class)
    fun getSingleSchedule(networkCode: Int, stop: TimeoStop): TimeoStopSchedule {
        val schedules = getMultipleSchedules(networkCode, listOf(stop))

        if (schedules.size > 0) {
            return schedules[0]
        } else {
            throw TimeoException("No schedules were returned.")
        }
    }

    /**
     * Fetches the next bus schedules for the specified list of bus stops.
     */
    @Throws(TimeoException::class, IOException::class)
    fun getMultipleSchedules(networkCode: Int, stops: List<TimeoStop>): List<TimeoStopSchedule> {
        // If no stops are in the list or all refs are null
        if (stops.all { stop -> stop.reference == null }) {
            return listOf()
        }

        // Append the stop references to the refs to send to the API
        var refs = stops.filter { stop -> stop.reference != null }
                .fold("", { refs, stop -> refs + stop.reference + ";"})

        // Don't keep the last semicolon
        refs = refs.substring(0, refs.length - 1)

        val params = "xml=3&refs=${URLEncoder.encode(refs, "UTF-8")}&ran=1"

        val result = http.requestWebPage(getEndpointUrl(networkCode), params, false)
        val res: ListeHorairesDTO = serializer.read(ListeHorairesDTO::class.java, result) ?: throw TimeoException("Service returned invalid data")

        checkForErrors(res.erreur)

        val schedules = res.horaires.map {
            horaire ->
            TimeoStopSchedule(
                    stop = stops.filter {
                        stop ->
                        stop.id == horaire.description.code
                                && stop.line.id == horaire.description.ligne
                                && stop.line.direction.id == horaire.description.sens
                    }.first(),
                    schedules = horaire.passages.map {
                        passage ->
                        TimeoSingleSchedule(
                                scheduleTime = passage.duree!!.getNextDateForTime(),
                                direction = passage.destination!!.smartCapitalize()
                        )
                    }
            )
        }

        return schedules
    }

    /**
     * Checks if there are outdated stops amongst those in the database,
     * by comparing them to a list of schedules returned by the API.
     */
    @Throws(TimeoException::class)
    fun checkForOutdatedStops(stops: List<TimeoStop>, schedules: List<TimeoStopSchedule>): Int {
        if (stops.size === schedules.size) {
            return 0
        }

        var count = 0
        for (stop in stops) {
            var outdated = true

            if (stop.reference == null) {
                count++
            }

            for (schedule in schedules.filter { schedule -> schedule.stop.id == stop.id }) {
                if (schedule.stop.id == stop.id) {
                    outdated = false
                    count++
                    break
                }
            }

            stop.isOutdated = outdated
        }

        return count
    }

    /**
     * Checks the DTO returned by the API for eventual server-side errors.
     * Throws an exception if one is encountered.
     */
    @Throws(TimeoException::class)
    private fun checkForErrors(error: ErreurDTO?) {
        if (error?.code != "000") {
            throw TimeoException(errorCode = error!!.code, message = error.message)
        }
    }

    /**
     * Fetches the current global traffic alert message.
     */
    val globalTrafficAlert: TimeoTrafficAlert?
        get() {
            try {
                val source = http.requestWebPage(PRE_HOME_URL, useCaches = true)

                if (!source.isEmpty()) {
                    val obj = JSONTokener(source).nextValue() as JSONObject

                    if (obj.has("alerte")) {
                        val alert = obj.getJSONObject("alerte")
                        return TimeoTrafficAlert(
                                alert.getInt("id_alerte"),
                                alert.getString("libelle_alerte").trim { it <= ' ' }.replace("  ".toRegex(), " - "),
                                alert.getString("url_alerte"))
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

            return null
        }

    /**
     * Gets the list of the supported bus networks.
     */
    val networksList: Map<Int, String>
        get() {
            return mapOf(
                    Pair(105, "Le Mans"),
                    Pair(117, "Pau"),
                    Pair(120, "Soissons"),
                    Pair(135, "Aix-en-Provence"),
                    Pair(147, "Caen"),
                    Pair(217, "Dijon"),
                    Pair(297, "Brest"),
                    Pair(402, "Pau-Agen"),
                    Pair(416, "Blois"),
                    Pair(422, "Saint-Étienne"),
                    Pair(440, "Nantes"),
                    Pair(457, "Montargis"),
                    Pair(497, "Angers"),
                    Pair(691, "Macon-Villefranche"),
                    Pair(910, "Épinay-sur-Orge"),
                    Pair(999, "Rennes")
            )
        }

    companion object {

        val TAG = TimeoRequestHandler::class.java.simpleName!!

        val DEFAULT_NETWORK_CODE = 147

        val API_BASE_URL = "http://timeo3.keolis.com/relais/"
        val PRE_HOME_URL = "http://twisto.fr/module/mobile/App2014/utils/getPreHome.php"

        /**
         * Returns the API endpoint for a given network code.
         *
         * @param networkCode the code for the city's bus network
         * @return the name of the page that has to be called for this specific network
         */
        private fun getEndpointUrl(networkCode: Int): String {
            return "$API_BASE_URL$networkCode.php"
        }
    }

}
