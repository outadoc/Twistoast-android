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

package fr.outadev.twistoast;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

/**
 * Created by outadoc on 1/2/15.
 */
public class ThemedActivity extends AppCompatActivity {

    private static HashMap<Integer, Integer> sThemes = new HashMap<>();

    static {
        sThemes.put(Color.parseColor("#C41915"), R.style.Twistoast_Theme_Red1);
        sThemes.put(Color.parseColor("#CA4318"), R.style.Twistoast_Theme_Red2);
        sThemes.put(Color.parseColor("#EC6C20"), R.style.Twistoast_Theme_Orange1);
        sThemes.put(Color.parseColor("#FD8A26"), R.style.Twistoast_Theme_Orange2);
        sThemes.put(Color.parseColor("#9ACA28"), R.style.Twistoast_Theme_Green1);
        sThemes.put(Color.parseColor("#C8B654"), R.style.Twistoast_Theme_Orange3);
        sThemes.put(Color.parseColor("#C79C38"), R.style.Twistoast_Theme_Orange4);
        sThemes.put(Color.parseColor("#CA7620"), R.style.Twistoast_Theme_Orange5);
        sThemes.put(Color.parseColor("#87B043"), R.style.Twistoast_Theme_Green2);
        sThemes.put(Color.parseColor("#53A527"), R.style.Twistoast_Theme_Green3);
        sThemes.put(Color.parseColor("#67971B"), R.style.Twistoast_Theme_Green4);
        sThemes.put(Color.parseColor("#13874B"), R.style.Twistoast_Theme_Green5);
        sThemes.put(Color.parseColor("#20AA66"), R.style.Twistoast_Theme_Green6);
        sThemes.put(Color.parseColor("#66B393"), R.style.Twistoast_Theme_Blue1);
        sThemes.put(Color.parseColor("#1897A6"), R.style.Twistoast_Theme_Blue2);
        sThemes.put(Color.parseColor("#70B3B8"), R.style.Twistoast_Theme_Blue3);
        sThemes.put(Color.parseColor("#3CB6E3"), R.style.Twistoast_Theme_Blue4);
        sThemes.put(Color.parseColor("#199ACA"), R.style.Twistoast_Theme_Blue5);
        sThemes.put(Color.parseColor("#1993E7"), R.style.Twistoast_Theme_Blue6);
        sThemes.put(Color.parseColor("#1789CE"), R.style.Twistoast_Theme_Blue7);
        sThemes.put(Color.parseColor("#8266C9"), R.style.Twistoast_Theme_Purple1);
        sThemes.put(Color.parseColor("#754CB2"), R.style.Twistoast_Theme_Purple2);
        sThemes.put(Color.parseColor("#6568C9"), R.style.Twistoast_Theme_Purple3);
        sThemes.put(Color.parseColor("#1A57B6"), R.style.Twistoast_Theme_Blue8);
        sThemes.put(Color.parseColor("#A969CA"), R.style.Twistoast_Theme_Purple4);
        sThemes.put(Color.parseColor("#983BCA"), R.style.Twistoast_Theme_Purple5);
        sThemes.put(Color.parseColor("#9D44B6"), R.style.Twistoast_Theme_Pink1);
        sThemes.put(Color.parseColor("#FC464B"), R.style.Twistoast_Theme_Red3);
        sThemes.put(Color.parseColor("#A29497"), R.style.Twistoast_Theme_Brown1);
        sThemes.put(Color.parseColor("#A37C82"), R.style.Twistoast_Theme_Brown2);
        sThemes.put(Color.parseColor("#E40068"), R.style.Twistoast_Theme_Pink3);
        sThemes.put(Color.parseColor("#C35E7E"), R.style.Twistoast_Theme_Pink2);
        sThemes.put(Color.parseColor("#424242"), R.style.Twistoast_Theme_Grey1);
        sThemes.put(Color.parseColor("#333333"), R.style.Twistoast_Theme_Grey2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigurationManager config = new ConfigurationManager(this);

        int themeRes = getThemeForColor(config.getApplicationThemeColor());
        setTheme(themeRes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Colors.getColorPrimaryDark(this));
        }
    }

    private int getThemeForColor(int themeColor) {
        if (sThemes.containsKey(themeColor)) {
            return sThemes.get(themeColor);
        }

        return R.style.Twistoast_Theme;
    }
}
