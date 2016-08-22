package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Element

class ArretDTO {
    @field:Element(required = false) var code: String = "000"
    @field:Element(required = false) var nom: String = ""
}