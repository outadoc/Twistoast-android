package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

/**
 * Represents the details of a stop and line in the XML API.
 *
 * @author outadoc
 */
class DescriptionDTO {
    @field:Element var code: Int = -1
    @field:Element lateinit var arret: String
    @field:Element lateinit var ligne: String
    @field:Element lateinit var ligne_nom: String
    @field:Element lateinit var sens: String
    @field:Element lateinit var vers: String
    @field:Element var couleur: String? = null
}

