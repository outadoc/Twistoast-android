package fr.outadev.android.transport

import java.io.IOException

/**
 * An interface defining functions for requesting a web page.
 *
 * @author outadoc
 */
interface IHttpRequester {

    @Throws(IOException::class)
    fun requestWebPage(url: String, params: String = "", useCaches: Boolean): String
}