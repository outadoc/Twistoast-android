package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element

/**
 * Represents a traffic info message in the XML API.
 *
 * @author outadoc
 */
class MessageDTO {
    @field:Attribute(required = false) var id: Int = -1
    @field:Attribute(required = false) var type: String? = null
    @field:Element(required = false) var titre: String? = null
    @field:Element(required = false) var texte: String? = null
    @field:Element var bloquant: Boolean = false
}

