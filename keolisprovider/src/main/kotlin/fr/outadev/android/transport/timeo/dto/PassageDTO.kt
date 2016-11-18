/*
 * Twistoast - PassageDTO.kt
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

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element

/**
 * Represents a bus schedule time in the XML API.
 *
 * @author outadoc
 */
class PassageDTO {
    @field:Attribute(name = "id") var id: Int = -1
    @field:Element(name = "duree") var duree: String? = null
    @field:Element(name = "destination") var destination: String? = null
}

