/*
 * Twistoast - DescriptionDTO.kt
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

package fr.outadev.android.transport.dto

import org.simpleframework.xml.Element

/**
 * Represents the details of a stop and line in the XML API.
 *
 * @author outadoc
 */
class DescriptionDTO {
    @field:Element(name = "code") var code: Int = -1
    @field:Element(name = "arret") lateinit var arret: String
    @field:Element(name = "ligne") lateinit var ligne: String
    @field:Element(name = "ligne_nom") lateinit var ligne_nom: String
    @field:Element(name = "sens") lateinit var sens: String
    @field:Element(name = "vers", required = false) var vers: String? = null
    @field:Element(name = "couleur") var couleur: String? = null
}

