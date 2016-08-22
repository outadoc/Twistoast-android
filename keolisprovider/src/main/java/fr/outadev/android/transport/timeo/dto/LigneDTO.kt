/*
 * Twistoast - LigneDTO.kt
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

package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

/**
 * Represents a bus line returned by the XML API.
 *
 * @author outadoc
 */
class LigneDTO {
    @field:Element lateinit var code: String
    @field:Element lateinit var nom: String
    @field:Element lateinit var sens: String
    @field:Element lateinit var vers: String
    @field:Element var couleur: Int = 0x34495E
}
