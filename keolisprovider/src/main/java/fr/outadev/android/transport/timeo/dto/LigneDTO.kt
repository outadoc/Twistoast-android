package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

class LigneDTO {
    @field:Element lateinit var code: String
    @field:Element lateinit var nom: String
    @field:Element lateinit var sens: String
    @field:Element lateinit var vers: String
    @field:Element var couleur: Int = 0x34495E
}
