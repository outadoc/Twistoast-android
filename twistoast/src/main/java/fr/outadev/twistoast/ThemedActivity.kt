/*
 * Twistoast - ThemedActivity
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
            Pair(parseColor("#C41915"), R.style.Twistoast_Theme_Red1),
            Pair(parseColor("#CA4318"), R.style.Twistoast_Theme_Red2),
            Pair(parseColor("#EC6C20"), R.style.Twistoast_Theme_Orange1),
            Pair(parseColor("#FD8A26"), R.style.Twistoast_Theme_Orange2),
            Pair(parseColor("#9ACA28"), R.style.Twistoast_Theme_Green1),
            Pair(parseColor("#C8B654"), R.style.Twistoast_Theme_Orange3),
            Pair(parseColor("#C79C38"), R.style.Twistoast_Theme_Orange4),
            Pair(parseColor("#CA7620"), R.style.Twistoast_Theme_Orange5),
            Pair(parseColor("#87B043"), R.style.Twistoast_Theme_Green2),
            Pair(parseColor("#53A527"), R.style.Twistoast_Theme_Green3),
            Pair(parseColor("#67971B"), R.style.Twistoast_Theme_Green4),
            Pair(parseColor("#13874B"), R.style.Twistoast_Theme_Green5),
            Pair(parseColor("#20AA66"), R.style.Twistoast_Theme_Green6),
            Pair(parseColor("#66B393"), R.style.Twistoast_Theme_Blue1),
            Pair(parseColor("#1897A6"), R.style.Twistoast_Theme_Blue2),
            Pair(parseColor("#70B3B8"), R.style.Twistoast_Theme_Blue3),
            Pair(parseColor("#3CB6E3"), R.style.Twistoast_Theme_Blue4),
            Pair(parseColor("#199ACA"), R.style.Twistoast_Theme_Blue5),
            Pair(parseColor("#1993E7"), R.style.Twistoast_Theme_Blue6),
            Pair(parseColor("#1789CE"), R.style.Twistoast_Theme_Blue7),
            Pair(parseColor("#8266C9"), R.style.Twistoast_Theme_Purple1),
            Pair(parseColor("#754CB2"), R.style.Twistoast_Theme_Purple2),
            Pair(parseColor("#6568C9"), R.style.Twistoast_Theme_Purple3),
            Pair(parseColor("#1A57B6"), R.style.Twistoast_Theme_Blue8),
            Pair(parseColor("#A969CA"), R.style.Twistoast_Theme_Purple4),
            Pair(parseColor("#983BCA"), R.style.Twistoast_Theme_Purple5),
            Pair(parseColor("#9D44B6"), R.style.Twistoast_Theme_Pink1),
            Pair(parseColor("#FC464B"), R.style.Twistoast_Theme_Red3),
            Pair(parseColor("#A29497"), R.style.Twistoast_Theme_Brown1),
            Pair(parseColor("#A37C82"), R.style.Twistoast_Theme_Brown2),
            Pair(parseColor("#E40068"), R.style.Twistoast_Theme_Pink3),
            Pair(parseColor("#C35E7E"), R.style.Twistoast_Theme_Pink2),
            Pair(parseColor("#424242"), R.style.Twistoast_Theme_Grey1),
            Pair(parseColor("#333333"), R.style.Twistoast_Theme_Grey2)
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
