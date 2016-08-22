package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList

/**
 * Represents a list of schedules returned by the XML API.
 *
 * @author outadoc
 */
class HoraireDTO {
    @field:Attribute var id: Int = -1
    @field:Element lateinit var description: DescriptionDTO
    @field:ElementList var passages: List<PassageDTO> = mutableListOf()
    @field:ElementList var messages: List<MessageDTO> = mutableListOf()
}
