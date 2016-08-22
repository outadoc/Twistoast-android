package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList

/**
 * Represents a list of lines or stops in the XML API (xml=1 root node).
 *
 * @author outadoc
 */
class ListeLignesDTO {
    @field:Element var erreur: ErreurDTO? = null
    @field:Element var heure: String? = null
    @field:Element var date: String? = null
    @field:Element var expire: String? = null
    @field:ElementList var alss: List<AlsDTO> = mutableListOf()
}