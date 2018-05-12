/*
 * Twistoast - ThemedActivity.kt
 * Copyright (C) 2013-2016 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast

import android.graphics.Color.parseColor
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import fr.outadev.twistoast.extensions.getColorPrimaryDark

/**
 * Created by outadoc on 1/2/15.
 */
open class ThemedActivity : AppCompatActivity() {

    private val themes = mapOf(
            parseColor("#C41915") to R.style.Twistoast_Theme_Red1,
            parseColor("#CA4318") to R.style.Twistoast_Theme_Red2,
            parseColor("#EC6C20") to R.style.Twistoast_Theme_Orange1,
            parseColor("#FD8A26") to R.style.Twistoast_Theme_Orange2,
            parseColor("#9ACA28") to R.style.Twistoast_Theme_Green1,
            parseColor("#C8B654") to R.style.Twistoast_Theme_Orange3,
            parseColor("#C79C38") to R.style.Twistoast_Theme_Orange4,
            parseColor("#CA7620") to R.style.Twistoast_Theme_Orange5,
            parseColor("#87B043") to R.style.Twistoast_Theme_Green2,
            parseColor("#53A527") to R.style.Twistoast_Theme_Green3,
            parseColor("#67971B") to R.style.Twistoast_Theme_Green4,
            parseColor("#13874B") to R.style.Twistoast_Theme_Green5,
            parseColor("#20AA66") to R.style.Twistoast_Theme_Green6,
            parseColor("#66B393") to R.style.Twistoast_Theme_Blue1,
            parseColor("#1897A6") to R.style.Twistoast_Theme_Blue2,
            parseColor("#70B3B8") to R.style.Twistoast_Theme_Blue3,
            parseColor("#3CB6E3") to R.style.Twistoast_Theme_Blue4,
            parseColor("#199ACA") to R.style.Twistoast_Theme_Blue5,
            parseColor("#1993E7") to R.style.Twistoast_Theme_Blue6,
            parseColor("#1789CE") to R.style.Twistoast_Theme_Blue7,
            parseColor("#8266C9") to R.style.Twistoast_Theme_Purple1,
            parseColor("#754CB2") to R.style.Twistoast_Theme_Purple2,
            parseColor("#6568C9") to R.style.Twistoast_Theme_Purple3,
            parseColor("#1A57B6") to R.style.Twistoast_Theme_Blue8,
            parseColor("#A969CA") to R.style.Twistoast_Theme_Purple4,
            parseColor("#983BCA") to R.style.Twistoast_Theme_Purple5,
            parseColor("#9D44B6") to R.style.Twistoast_Theme_Pink1,
            parseColor("#FC464B") to R.style.Twistoast_Theme_Red3,
            parseColor("#A29497") to R.style.Twistoast_Theme_Brown1,
            parseColor("#A37C82") to R.style.Twistoast_Theme_Brown2,
            parseColor("#E40068") to R.style.Twistoast_Theme_Pink3,
            parseColor("#C35E7E") to R.style.Twistoast_Theme_Pink2,
            parseColor("#424242") to R.style.Twistoast_Theme_Grey1,
            parseColor("#333333") to R.style.Twistoast_Theme_Grey2
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = ConfigurationManager(this)
        setTheme(getThemeForColor(config.applicationThemeColor))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = getColorPrimaryDark()
        }
    }

    private fun getThemeForColor(themeColor: Int): Int {
        return themes[themeColor] ?: R.style.Twistoast_Theme
    }
}
