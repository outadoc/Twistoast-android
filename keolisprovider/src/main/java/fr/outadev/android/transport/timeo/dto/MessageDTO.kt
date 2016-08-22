package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

/**
 * Created by outadoc on 21/08/16.
 */
class MessageDTO {
    @field:Element(required = false) var titre: String? = null
    @field:Element(required = false) var texte: String? = null
    @field:Element var bloquant: Boolean = false
}

