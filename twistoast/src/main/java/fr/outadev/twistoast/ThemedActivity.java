/*
 * Twistoast - ThemedActivity
 * Copyright (C) 2013-2015  Baptiste Candellier
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

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

/**
 * Created by outadoc on 1/2/15.
 */
public class ThemedActivity extends AppCompatActivity {

	private static HashMap<Integer, Integer> themes = new HashMap<>();

	static {
		themes.put(Color.parseColor("#C41915"), R.style.Theme_C41915);
		themes.put(Color.parseColor("#CA4318"), R.style.Theme_CA4318);
		themes.put(Color.parseColor("#EC6C20"), R.style.Theme_EC6C20);
		themes.put(Color.parseColor("#FD8A26"), R.style.Theme_FD8A26);
		themes.put(Color.parseColor("#9ACA28"), R.style.Theme_9ACA28);
		themes.put(Color.parseColor("#C8B654"), R.style.Theme_C8B654);
		themes.put(Color.parseColor("#C79C38"), R.style.Theme_C79C38);
		themes.put(Color.parseColor("#CA7620"), R.style.Theme_CA7620);
		themes.put(Color.parseColor("#87B043"), R.style.Theme_87B043);
		themes.put(Color.parseColor("#53A527"), R.style.Theme_53A527);
		themes.put(Color.parseColor("#67971B"), R.style.Theme_67971B);
		themes.put(Color.parseColor("#13874B"), R.style.Theme_13874B);
		themes.put(Color.parseColor("#20AA66"), R.style.Theme_20AA66);
		themes.put(Color.parseColor("#66B393"), R.style.Theme_66B393);
		themes.put(Color.parseColor("#1897A6"), R.style.Theme_1897A6);
		themes.put(Color.parseColor("#70B3B8"), R.style.Theme_70B3B8);
		themes.put(Color.parseColor("#3CB6E3"), R.style.Theme_3CB6E3);
		themes.put(Color.parseColor("#199ACA"), R.style.Theme_199ACA);
		themes.put(Color.parseColor("#1993E7"), R.style.Theme_1993E7);
		themes.put(Color.parseColor("#1789CE"), R.style.Theme_1789CE);
		themes.put(Color.parseColor("#8266C9"), R.style.Theme_8266C9);
		themes.put(Color.parseColor("#754CB2"), R.style.Theme_754CB2);
		themes.put(Color.parseColor("#6568C9"), R.style.Theme_6568C9);
		themes.put(Color.parseColor("#1A57B6"), R.style.Theme_1A57B6);
		themes.put(Color.parseColor("#A969CA"), R.style.Theme_A969CA);
		themes.put(Color.parseColor("#983BCA"), R.style.Theme_983BCA);
		themes.put(Color.parseColor("#9D44B6"), R.style.Theme_9D44B6);
		themes.put(Color.parseColor("#FC464B"), R.style.Theme_FC464B);
		themes.put(Color.parseColor("#A29497"), R.style.Theme_A29497);
		themes.put(Color.parseColor("#A37C82"), R.style.Theme_A37C82);
		themes.put(Color.parseColor("#E40068"), R.style.Theme_E40068);
		themes.put(Color.parseColor("#C35E7E"), R.style.Theme_C35E7E);
		themes.put(Color.parseColor("#424242"), R.style.Theme_424242);
		themes.put(Color.parseColor("#333333"), R.style.Theme_333333);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		int themeColor = prefs.getInt("pref_app_theme", -1);
		int themeRes = getThemeForColor(themeColor);

		setTheme(themeRes);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(Colors.getColorPrimaryDark(this));
		}
	}

	private int getThemeForColor(int themeColor) {
		if(themes.containsKey(themeColor)) {
			return themes.get(themeColor);
		}

		return R.style.AppTheme;
	}
}
