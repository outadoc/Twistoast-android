package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

/**
 * Created by outadoc on 21/08/16.
 */
class DescriptionDTO {
    @field:Element var code: Int = -1
    @field:Element var arret: String? = null
    @field:Element var ligne: String? = null
    @field:Element var ligne_nom: String? = null
    @field:Element var sens: String? = null
    @field:Element var vers: String? = null
    @field:Element var couleur: String? = null
}

