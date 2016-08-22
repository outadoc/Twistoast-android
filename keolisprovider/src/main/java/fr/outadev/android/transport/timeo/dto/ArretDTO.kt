package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

class ArretDTO {
    @field:Element(required = false) var code: String? = null
    @field:Element(required = false) var nom: String? = null
}