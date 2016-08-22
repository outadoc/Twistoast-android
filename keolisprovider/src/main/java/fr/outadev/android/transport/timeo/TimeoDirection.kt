package fr.outadev.android.transport.timeo

/**
 * Represents the direction of a bus line.
 * Id can be either A or R (in theory, but it's not restricted).
 */
data class TimeoDirection (var id: String, var name: String) {

    override fun toString(): String = "$id - $name"
}