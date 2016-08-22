package fr.outadev.android.transport

import java.io.IOException

/**
 * Created by outadoc on 22/08/16.
 */
interface IHttpRequester {

    @Throws(IOException::class)
    fun requestWebPage(url: String, params: String = "", useCaches: Boolean): String
}