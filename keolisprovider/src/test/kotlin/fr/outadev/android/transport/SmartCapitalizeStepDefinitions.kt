/*
 * Twistoast - SmartCapitalizeStepDefinitions.kt
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

package fr.outadev.android.transport

/*
 * Twistoast - fr.outadev.android.transport.timeo.SmartCapitalizeStepDefinitions.java
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

import cucumber.api.CucumberOptions
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import fr.outadev.android.transport.smartCapitalize
import org.junit.Assert.assertEquals

/**
 * Created by outadoc on 26/08/16.
 */
@CucumberOptions
class SmartCapitalizeStepDefinitions {

    var capitalizedWord: String? = null

    @When("^I capitalize '(.+)'$")
    fun iCapitalize(word: String) {
        capitalizedWord = word.smartCapitalize()
    }

    @Then("^I get '(.+)'$")
    fun iGet(expected: String) {
        assertEquals(expected, capitalizedWord)
    }

}
