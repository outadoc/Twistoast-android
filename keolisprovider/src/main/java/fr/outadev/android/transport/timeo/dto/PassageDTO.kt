package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element

/**
 * Created by outadoc on 21/08/16.
 */
class PassageDTO {
    @field:Attribute var id: Int = -1
    @field:Element var duree: String? = null
    @field:Element var destination: String? = null
}

