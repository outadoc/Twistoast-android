package fr.outadev.android.transport.timeo.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Text

/**
 * Created by outadoc on 21/08/16.
 */
class ErreurDTO {
    @field:Attribute var code: String = "000"
    @field:Text(required = false) var message: String = ""
}
