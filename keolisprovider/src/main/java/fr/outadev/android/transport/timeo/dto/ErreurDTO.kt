package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Text

/**
 * Represents an error returned by the XML API.
 *
 * @author outadoc
 */
class ErreurDTO {
    @field:Attribute var code: String = "000"
    @field:Text(required = false) var message: String = ""
}
