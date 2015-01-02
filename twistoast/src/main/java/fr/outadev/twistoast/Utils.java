/*
 * Twistoast - Utils
 * Copyright (C) 2013-2014  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

/**
 * Miscellaneous methods.
 *
 * @author outadoc
 */
public abstract class Utils {

	public static final String TAG = "Twistoast";

	public static int getColorPrimary(Context context) {
		return getColorFromAttribute(context, R.attr.colorPrimary);
	}

	public static int getColorPrimaryDark(Context context) {
		return getColorFromAttribute(context, R.attr.colorPrimaryDark);
	}

	public static int getColorAccent(Context context) {
		return getColorFromAttribute(context, R.attr.colorAccent);
	}

	private static int getColorFromAttribute(Context context, @AttrRes int attr) {
		TypedValue a = new TypedValue();
		context.getTheme().resolveAttribute(attr, a, true);

		if(a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			return a.data;
		}

		throw new RuntimeException("Attribute is not a color.");
	}

}
