/*
 * Twistoast - KeolisDao.kt
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
 * Twistoast - KeolisDaoyright (C) 2013-2018 Baptiste Candellier
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

package fr.outadev.android.transport

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.github.kittinunf.fuel.Fuel
import fr.outadev.android.transport.dto.ErreurDTO
import fr.outadev.android.transport.dto.ListeHorairesDTO
import fr.outadev.android.transport.dto.ListeLignesDTO
import fr.outadev.android.transport.dto.MessageDTO
import fr.outadev.twistoast.model.*
import fr.outadev.twistoast.model.Result.Companion.failure
import fr.outadev.twistoast.model.Result.Companion.loading
import fr.outadev.twistoast.model.Result.Companion.success
import org.apache.commons.lang3.StringUtils.leftPad
import org.simpleframework.xml.core.Persister
import java.net.URLEncoder
import com.github.kittinunf.result.Result.Failure as FuelFailure
import com.github.kittinunf.result.Result.Success as FuelSuccess

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
class KeolisDao {

    private val serializer: Persister = Persister()

    /**
     * Fetches the bus stops for the specified line.
     */
    fun getStops(line: Line): LiveData<Result<List<Stop>>> {
        return getStops(line.networkCode, line)
    }

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    fun getSingleSchedule(stop: Stop): LiveData<Result<StopSchedule>> {
        return getSingleSchedule(stop.line.networkCode, stop)
    }

    fun getLines(networkCode: Int = DEFAULT_NETWORK_CODE): LiveData<Result<List<Line>>> {
        val params = listOf("1" to "xml")

        return httpGet(networkCode, params, ListeLignesDTO::class.java) { live, res ->
            live.value = success(
                    res.alss.map { als ->
                        Line(
                                id = als.ligne.code,
                                name = als.ligne.nom.smartCapitalize(),
                                direction = Direction(als.ligne.sens, als.ligne.vers?.smartCapitalize()),
                                color = "#" + leftPad(Integer.toHexString(Integer.valueOf(als.ligne.couleur)), 6, '0'),
                                networkCode = networkCode)
                    })
        }
    }

    private fun <T : Any?, U : Any?> httpGet(networkCode: Int, params: List<Pair<String, String>>, serialClass: Class<U>, callback: (MutableLiveData<Result<T>>, U) -> Unit): LiveData<Result<T>> {
        val live = MutableLiveData<Result<T>>()

        Fuel.get(getEndpointUrl(networkCode), params).responseString { _, _, result ->
            when (result) {
                is FuelSuccess -> {
                    val cleanResult = result.value.replace(" {6}<description>\n {8}<code></code>\n {8}<arret></arret>\n {8}<ligne></ligne>\n {8}<ligne_nom></ligne_nom>\n {8}<sens></sens>\n {8}<vers></vers>\n {8}<couleur>#000000</couleur>\n {6}</description>".toRegex(), "")
                    val res = serializer.read(serialClass, cleanResult)

                    when (res) {
                        null -> live.value = failure(DataProviderException("Service returned invalid data"))
                        else -> {
                            when (res) {
                                is ListeHorairesDTO -> try {
                                    checkForErrors(res.erreur)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    live.value = failure(e)
                                }
                            }

                            callback(live, res)
                        }
                    }
                }

                is FuelFailure -> live.value = failure(result.error)
            }
        }

        return live
    }

    fun getStops(networkCode: Int, line: Line): LiveData<Result<List<Stop>>> {
        val params = listOf(
                "1" to "xml",
                line.id to "ligne",
                line.direction.id to "sens"
        )

        return httpGet(networkCode, params, ListeLignesDTO::class.java) { liveData, res ->
            liveData.value = success(res.alss
                    .filter { it.arret.code != null && it.arret.nom != null }
                    .map { als ->
                        Stop(
                                id = als.arret.code!!.toInt(),
                                name = als.arret.nom!!.smartCapitalize(),
                                reference = als.refs,
                                line = Line(
                                        id = als.ligne.code,
                                        name = als.ligne.nom.smartCapitalize(),
                                        direction = Direction(als.ligne.sens, als.ligne.vers?.smartCapitalize()),
                                        color = "#" + leftPad(Integer.toHexString(Integer.valueOf(als.ligne.couleur)), 6, '0'),
                                        networkCode = networkCode))
                    })
        }
    }

    /**
     * Retrieve a list of stops by their code.
     * Useful to get a stop's info when they're only known by their code.
     */
    fun getStopsByCode(networkCode: Int = DEFAULT_NETWORK_CODE, codes: List<Int>): LiveData<Result<List<Stop>>> {
        val codesCat = codes
                .filterNot { code -> code == 0 }
                .joinToString(",")

        val params = listOf("1" to "xml", codesCat to "code")

        return httpGet(networkCode, params, ListeLignesDTO::class.java) { liveData, res ->
            liveData.value = success(res.alss
                    .filter { it.arret.code != null }
                    .filter { it.arret.nom != null }
                    .map { als ->
                        Stop(
                                id = als.arret.code!!.toInt(),
                                name = als.arret.nom!!.smartCapitalize(),
                                reference = als.refs,
                                line = Line(
                                        id = als.ligne.code,
                                        name = als.ligne.nom.smartCapitalize(),
                                        direction = Direction(als.ligne.sens, als.ligne.vers?.smartCapitalize()),
                                        color = "#" + leftPad(Integer.toHexString(Integer.valueOf(als.ligne.couleur)), 6, '0'),
                                        networkCode = networkCode))
                    })
        }
    }

    /**
     * Fetches the next bus schedules for the specified bus stop.
     */
    fun getSingleSchedule(networkCode: Int, stop: Stop): LiveData<Result<StopSchedule>> {
        return Transformations.map(getMultipleSchedules(networkCode, listOf(stop))) { schedules ->
            when (schedules) {
                is Result.Success -> {
                    success(schedules.data[0])
                }

                is Result.Failure -> failure(schedules.e)
                is Result.Loading -> loading(schedules.loading)
            }
        }
    }

    /**
     * Fetches the next bus schedules for the specified list of bus stops.
     */
    fun getMultipleSchedules(networkCode: Int, stops: List<Stop>): LiveData<Result<List<StopSchedule>>> {
        // If no stops are in the list or all refs are null
        if (stops.all { stop -> stop.reference == null }) {
            val live = MutableLiveData<Result<List<StopSchedule>>>()
            live.value = success(emptyList())
            return live
        }

        // Append the stop references to the refs to send to the API
        val refs = stops.filter { stop -> stop.reference != null }
                .map { stop -> stop.reference }
                .joinToString(";")

        val params = listOf(
                "3" to "xml",
                URLEncoder.encode(refs, "UTF-8") to "refs",
                "1" to "ran"
        )

        return httpGet(networkCode, params, ListeHorairesDTO::class.java) { liveData, res ->
            liveData.value = success(
                    res.horaires
                            .filter { horaire ->
                                horaire.description != null && stops.any { stop ->
                                    stop.id == horaire.description?.code
                                            && stop.line.id == horaire.description!!.ligne
                                            && stop.line.direction.id == horaire.description!!.sens
                                }
                            }.map { horaire ->
                                StopSchedule(
                                        // Retrieve the stop corresponding to this schedule in the list that was passed
                                        // to the API, so we can match them
                                        stop = stops.first { stop ->
                                            stop.id == horaire.description?.code
                                                    && stop.line.id == horaire.description?.ligne
                                                    && stop.line.direction.id == horaire.description?.sens
                                        },
                                        // Parse the bus schedules
                                        schedules = horaire.passages.map { passage ->
                                            ScheduledArrival(
                                                    scheduleTime = passage.duree!!.getNextDateForTime(),
                                                    direction = passage.destination!!.smartCapitalize()
                                            )
                                        },
                                        // Retrieve the traffic message(s) for this bus stop
                                        trafficMessages = horaire.messages.filter { it.titre != null }.map { message ->
                                            StopTrafficMessage(
                                                    title = message.titre!!.trim(),
                                                    body = message.texte?.trim()!!.replace("  ", " ")
                                            )
                                        }.distinct()
                                )
                            })
        }
    }

    /**
     * Checks if there are outdated stops amongst those in the database,
     * by comparing them to a list of schedules returned by the API.
     */
    fun checkForOutdatedStops(stops: List<Stop>, schedules: List<StopSchedule>): Result<Int> {
        if (stops.size == schedules.size) {
            return success(0)
        }

        return success(
                stops.fold(0) { outdated, stop ->
                    if (stop.reference == null || schedules.none { schedule -> schedule.stop.id == stop.id }) {
                        stop.isOutdated = true
                        outdated + 1
                    } else {
                        outdated
                    }
                })
    }

    /**
     * Checks the DTO returned by the API for eventual server-side errors.
     * Throws an exception if one is encountered.
     */
    private fun checkForErrors(error: ErreurDTO?, message: MessageDTO? = null) {
        if (error?.code != "000") {
            throw DataProviderException(errorCode = error!!.code, message = error.message)
        }

        if (message != null && message.bloquant && message.titre != null) {
            throw BlockingMessageException(message.titre!!, message.texte)
        }
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

        val TAG = KeolisDao::class.java.simpleName

        const val DEFAULT_NETWORK_CODE = 147

        const val API_BASE_URL = "http://timeo3.keolis.com/relais/"

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
