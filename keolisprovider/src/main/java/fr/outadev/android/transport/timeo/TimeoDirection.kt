package fr.outadev.android.transport.timeo

/**
 * Created by outadoc on 22/08/16.
 */
data class TimeoDirection (var id: String, var name: String) {

    override fun toString(): String = "$id - $name"
}