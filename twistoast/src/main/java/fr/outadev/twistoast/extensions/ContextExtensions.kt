/*
 * Twistoast - Colors
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

package fr.outadev.twistoast.extensions

import android.content.Context
import android.support.annotation.AttrRes
import android.util.TypedValue
import fr.outadev.twistoast.R

/**
 * Miscellaneous methods used to manipulate colours.
 *
 * @author outadoc
 */

fun Context.getColorPrimary(): Int = getColorFromAttribute(this, R.attr.colorPrimary)

fun Context.getColorPrimaryDark(): Int = getColorFromAttribute(this, R.attr.colorPrimaryDark)

fun Context.getColorAccent(): Int = getColorFromAttribute(this, R.attr.colorAccent)

private fun getColorFromAttribute(context: Context, @AttrRes attr: Int): Int {
    val a = TypedValue()
    context.theme.resolveAttribute(attr, a, true)

    if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
        return a.data
    }

    throw RuntimeException("Attribute is not a color.")
}

