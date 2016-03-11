/*
 * Twistoast - NavitiaLine
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

package fr.outadev.android.transport.keolis;

import android.graphics.Color;

import fr.outadev.android.transport.ILine;
import fr.outadev.android.transport.INetwork;

/**
 * Stores a bus line and its direction.
 *
 * @author outadoc
 */
public class NavitiaLine implements ILine {

    private int mId;
    private String mName;
    private int mColor;

    public NavitiaLine() {
        mId = -1;
        mColor = Color.parseColor("#34495E");
    }

    public NavitiaLine(int id, String name, int color) {
        this();

        mId = id;
        mName = name;
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    @Override
    public INetwork getNetwork() {
        return NavitiaNetwork.getInstance();
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

}
