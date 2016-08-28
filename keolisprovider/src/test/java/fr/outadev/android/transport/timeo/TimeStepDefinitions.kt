/*
 * Twistoast - TimeStepDefinitions.kt
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

import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import fr.outadev.android.transport.getNextDateForTime
import org.joda.time.LocalTime
import org.junit.Assert

/**
 * Created by outadoc on 27/08/16.
 */

class TimeStepDefinitions {

    var localTime: String? = null
    var busTime: String? = null

    @Given("^it's ([0-9]{2}:[0-9]{2})$")
    fun itsTime(time: String) {
        localTime = time
    }

    @When("^a bus is coming at ([0-9]{2}:[0-9]{2})$")
    fun aBusIsComingAtTime(time: String) {
        busTime = time
    }

    @Then("^the bus is coming in (\\d+) minutes$")
    fun theBusIsComingInNMinutes(min: Long) {
        Assert.assertEquals(min, getMinuteDiffBetweenTwoStringTimes(busTime!!, localTime!!))
    }

    @Then("^the bus is late by (\\d+) minutes$")
    fun theBusIsLateByNMinutes(min: Long) {
        Assert.assertEquals(-min, getMinuteDiffBetweenTwoStringTimes(busTime!!, localTime!!))
    }

    fun getMinuteDiffBetweenTwoStringTimes(busTime: String, localTime: String): Long {
        val localArray = localTime.split(":")
        val localDate = LocalTime(localArray[0].toInt(), localArray[1].toInt()).toDateTimeToday()

        val busDate = busTime.getNextDateForTime(localDate)

        return (busDate.millis - localDate.millis) / 1000 / 60
    }
}