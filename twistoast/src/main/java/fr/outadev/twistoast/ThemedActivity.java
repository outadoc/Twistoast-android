/*
 * Twistoast - ThemedActivity
 * Copyright (C) 2013-2015 Baptiste Candellier
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
		themes.put(Color.parseColor("#C41915"), R.style.Twistoast_Theme_Red1);
		themes.put(Color.parseColor("#CA4318"), R.style.Twistoast_Theme_Red2);
		themes.put(Color.parseColor("#EC6C20"), R.style.Twistoast_Theme_Orange1);
		themes.put(Color.parseColor("#FD8A26"), R.style.Twistoast_Theme_Orange2);
		themes.put(Color.parseColor("#9ACA28"), R.style.Twistoast_Theme_Green1);
		themes.put(Color.parseColor("#C8B654"), R.style.Twistoast_Theme_Orange3);
		themes.put(Color.parseColor("#C79C38"), R.style.Twistoast_Theme_Orange4);
		themes.put(Color.parseColor("#CA7620"), R.style.Twistoast_Theme_Orange5);
		themes.put(Color.parseColor("#87B043"), R.style.Twistoast_Theme_Green2);
		themes.put(Color.parseColor("#53A527"), R.style.Twistoast_Theme_Green3);
		themes.put(Color.parseColor("#67971B"), R.style.Twistoast_Theme_Green4);
		themes.put(Color.parseColor("#13874B"), R.style.Twistoast_Theme_Green5);
		themes.put(Color.parseColor("#20AA66"), R.style.Twistoast_Theme_Green6);
		themes.put(Color.parseColor("#66B393"), R.style.Twistoast_Theme_Blue1);
		themes.put(Color.parseColor("#1897A6"), R.style.Twistoast_Theme_Blue2);
		themes.put(Color.parseColor("#70B3B8"), R.style.Twistoast_Theme_Blue3);
		themes.put(Color.parseColor("#3CB6E3"), R.style.Twistoast_Theme_Blue4);
		themes.put(Color.parseColor("#199ACA"), R.style.Twistoast_Theme_Blue5);
		themes.put(Color.parseColor("#1993E7"), R.style.Twistoast_Theme_Blue6);
		themes.put(Color.parseColor("#1789CE"), R.style.Twistoast_Theme_Blue7);
		themes.put(Color.parseColor("#8266C9"), R.style.Twistoast_Theme_Purple1);
		themes.put(Color.parseColor("#754CB2"), R.style.Twistoast_Theme_Purple2);
		themes.put(Color.parseColor("#6568C9"), R.style.Twistoast_Theme_Purple3);
		themes.put(Color.parseColor("#1A57B6"), R.style.Twistoast_Theme_Blue8);
		themes.put(Color.parseColor("#A969CA"), R.style.Twistoast_Theme_Purple4);
		themes.put(Color.parseColor("#983BCA"), R.style.Twistoast_Theme_Purple5);
		themes.put(Color.parseColor("#9D44B6"), R.style.Twistoast_Theme_Pink1);
		themes.put(Color.parseColor("#FC464B"), R.style.Twistoast_Theme_Red3);
		themes.put(Color.parseColor("#A29497"), R.style.Twistoast_Theme_Brown1);
		themes.put(Color.parseColor("#A37C82"), R.style.Twistoast_Theme_Brown2);
		themes.put(Color.parseColor("#E40068"), R.style.Twistoast_Theme_Pink3);
		themes.put(Color.parseColor("#C35E7E"), R.style.Twistoast_Theme_Pink2);
		themes.put(Color.parseColor("#424242"), R.style.Twistoast_Theme_Grey1);
		themes.put(Color.parseColor("#333333"), R.style.Twistoast_Theme_Grey2);
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

		return R.style.Twistoast_Theme;
	}
}
