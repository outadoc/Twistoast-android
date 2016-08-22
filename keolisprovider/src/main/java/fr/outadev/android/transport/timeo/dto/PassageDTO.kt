package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element

/**
 * Represents a bus schedule time in the XML API.
 *
 * @author outadoc
 */
class PassageDTO {
    @field:Attribute var id: Int = -1
    @field:Element var duree: String? = null
    @field:Element var destination: String? = null
}

