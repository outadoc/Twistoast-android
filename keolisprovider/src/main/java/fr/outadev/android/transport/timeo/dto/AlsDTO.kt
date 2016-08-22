package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element

/**
 * Created by outadoc on 21/08/16.
 */
class AlsDTO {
    @field:Attribute var id: Int = -1
    @field:Element lateinit var arret: ArretDTO
    @field:Element lateinit var ligne: LigneDTO
    @field:Element(required = false) var refs: String? = null
}

