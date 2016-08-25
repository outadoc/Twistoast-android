/*
 * Twistoast - ParseStepDefinitions.kt
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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import cucumber.api.DataTable
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import fr.outadev.android.transport.IHttpRequester
import org.mockito.Mockito
import java.io.BufferedReader
import java.io.FileReader

/**
 * Created by outadoc on 23/08/16.
 */
class ParseStepDefinitions {

    lateinit var trh: TimeoRequestHandler

    var lines: List<TimeoLine>? = null

    @Given("^a connection to the API$")
    @Throws(Throwable::class)
    fun aConnectionToTheAPI() {
        val requester: IHttpRequester = Mockito.mock(IHttpRequester::class.java)

        whenever(requester.requestWebPage(any(), eq("xml=1"), any()))
                .thenReturn(readStringFromResFile("src/test/res/mocks/test_lines_list.xml"))

        trh = TimeoRequestHandler(requester)
    }

    @When("^I request a list of lines$")
    @Throws(Throwable::class)
    fun iRequestAListOfLines() {
        lines = trh.getLines()
    }

    @Then("^I get the following lines$")
    @Throws(Throwable::class)
    fun iGetTheFollowingLines(expected: DataTable) {
        var lolz = DataTable.create(lines)
        expected.diff(lines)
    }

    fun readStringFromResFile(filename: String): String {
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
