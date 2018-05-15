/*
 * Twistoast - Colors.kt
 * Copyright (C) 2013-2018 Baptiste Candellier
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

package fr.outadev.twistoast.extensions

import android.graphics.Color

/**
 * Miscellaneous methods used to manipulate colours.
 *
 * @author outadoc
 */

fun String.toColor(): Color? {
    return Color.valueOf(Color.parseColor(this))
}

fun Color.brighten(): Color {
    var color = this
    val hsv = FloatArray(3)

    if (color.toArgb() == Color.BLACK) {
        color = Color.valueOf(Color.parseColor("#404040"))
    }

    Color.colorToHSV(color.toArgb(), hsv)
    hsv[0] -= 35f
    hsv[2] *= 1.8f

    return Color.valueOf(Color.HSVToColor(hsv))
}
