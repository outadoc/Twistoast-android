/*
 * Twistoast - ParseStepDefinitions.kt
 * Copyright (C) 2013-2017 Baptiste Candellier
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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import cucumber.api.CucumberOptions
import cucumber.api.DataTable
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import fr.outadev.android.transport.IHttpRequester
import org.mockito.Mockito
import java.io.BufferedReader
import java.io.FileReader
import java.net.URLEncoder

/**
 * Created by outadoc on 23/08/16.
 */
@CucumberOptions
class ParseStepDefinitions {

    companion object {
        const val mockDir = "keolisprovider/src/test/resources/mocks/"
    }

    var requester: IHttpRequester? = null
    var trh: TimeoRequestHandler? = null

    var lines: List<TimeoLine>? = null
    var stops: List<TimeoStop>? = null

    @Given("^a connection to the API$")
    @Throws(Throwable::class)
    fun aConnectionToTheAPI() {
        requester = Mockito.mock(IHttpRequester::class.java)
        trh = TimeoRequestHandler(requester!!)
    }

    @When("^I request a list of lines with mock (.+)$")
    @Throws(Throwable::class)
    fun iRequestAListOfLinesWithMock(mock: String) {
        setupHttpMock("xml=1", mock)
        lines = trh!!.getLines()
    }

    @Then("^I get the following lines$")
    @Throws(Throwable::class)
    fun iGetTheFollowingLines(expected: DataTable) {
        expected.diff(lines)
    }

    @Then("^I get the following stops$")
    @Throws(Throwable::class)
    fun iGetTheFollowingStops(expected: DataTable) {
        expected.diff(stops)
    }

    @When("^I request a list of stops for line '(.+)' and direction '(A|R)' with mock (.+)$")
    @Throws(Throwable::class)
    fun iRequestAListOfStopsForLineAndDirectionWithMock(line: String, dir: String, mock: String) {
        setupHttpMock("xml=1&ligne=$line&sens=$dir", mock)
        stops = trh!!.getStops(TimeoLine(line, "", TimeoDirection(dir, "")))
    }

    @When("^I request a list of schedules for the following stop references with mock (.+)$")
    @Throws(Throwable::class)
    fun iRequestAListOfSchedulesForTheFollowingStopReferencesWithMock(mock: String, refs:List<String>) {
        var refsStr = refs.fold("", { refsStr, ref -> refsStr + ref + ";"})
        refsStr = refsStr.substring(0, refsStr.length - 1)
        setupHttpMock("xml=3&refs=${URLEncoder.encode(refsStr, "UTF-8")}&ran=1", mock)
    }

    @Then("^I get a list of schedules for the following stop$")
    fun i_get_a_list_of_schedules_for_the_following_stop(expected: DataTable) {
    }

    @Then("^the list of schedules is$")
    fun the_list_of_schedules_is(expected: DataTable) {
    }

    private fun setupHttpMock(params: String, mockFileName: String) {
        whenever(requester!!.requestWebPage(any(), eq(params), any()))
                .thenReturn(readStringFromResFile(mockDir + mockFileName))
    }

    private fun readStringFromResFile(filename: String): String {
        BufferedReader(FileReader(filename)).use({ br ->
            val sb = StringBuilder()
            var line = br.readLine()

            while (line != null) {
                sb.append(line)
                sb.append("\n")
                line = br.readLine()
            }

            return sb.toString()
        })
    }
}
