package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList

/**
 * Represents a list of schedules in the XML API (xml=3 root node).
 *
 * @author outadoc
 */
class ListeHorairesDTO {
    @field:Element var erreur: ErreurDTO? = null
    @field:Element var heure: String? = null
    @field:Element var date: String? = null
    @field:Element var reseau: MessageDTO? = null
    @field:ElementList var horaires: List<HoraireDTO> = mutableListOf()
}
