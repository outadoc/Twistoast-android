package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

class LigneDTO {
    @field:Element var code: String? = null
    @field:Element var nom: String? = null
    @field:Element var sens: String? = null
    @field:Element var vers: String? = null
    @field:Element var couleur: Int = 0x34495E
}
