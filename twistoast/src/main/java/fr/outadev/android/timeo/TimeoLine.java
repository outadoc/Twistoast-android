/*
 * Twistoast - TimeoLine
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

package fr.outadev.android.timeo;

/**
 * Stores a bus line and its direction.
 *
 * @author outadoc
 */
public class TimeoLine implements ITimeoIDName {

    private TimeoIDNameObject mLine;
    private TimeoIDNameObject mDirection;

    private String mColor;
    private int mNetworkCode = TimeoRequestHandler.DEFAULT_NETWORK_CODE;

    /**
     * Create a new line with line details, a direction, and a color.
     *
     * @param line        line details: id = line id, name = line name
     * @param direction   direction details
     * @param color       line color, as an HTML-like color string (e.g. #123456)
     * @param networkCode the identifier of the network this line is a part of (e.g. 147 for Twisto)
     */
    public TimeoLine(TimeoIDNameObject line, TimeoIDNameObject direction, String color, int networkCode) {
        this(line, direction, networkCode);
        this.mColor = color;
    }

    /**
     * Create a new line with line details, a direction, and a color.
     *
     * @param line        line details: id = line id, name = line name
     * @param direction   direction details
     * @param networkCode the identifier of the network this line is a part of (e.g. 147 for Twisto)
     */
    public TimeoLine(TimeoIDNameObject line, TimeoIDNameObject direction, int networkCode) {
        this.mLine = line;
        this.mDirection = direction;
        this.mNetworkCode = networkCode;
    }

    public TimeoIDNameObject getDetails() {
        return mLine;
    }

    public void setDetails(TimeoIDNameObject line) {
        this.mLine = line;
    }

    public TimeoIDNameObject getDirection() {
        return mDirection;
    }

    public void setDirection(TimeoIDNameObject direction) {
        this.mDirection = direction;
    }

    public String getColor() {
        return (mColor == null) ? "#34495E" : mColor;
    }

    public void setColor(String color) {
        this.mColor = color;
    }

    public int getNetworkCode() {
        return mNetworkCode;
    }

    public void setNetworkCode(int networkCode) {
        this.mNetworkCode = networkCode;
    }

    @Override
    public String getId() {
        return mLine.getId();
    }

    @Override
    public void setId(String id) {
        mLine.setId(id);
    }

    @Override
    public String getName() {
        return mLine.getName();
    }

    @Override
    public void setName(String name) {
        mLine.setName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimeoLine timeoLine = (TimeoLine) o;

        if (mNetworkCode != timeoLine.mNetworkCode) {
            return false;
        }

        if (mDirection != null ? !mDirection.equals(timeoLine.mDirection) : timeoLine.mDirection != null) {
            return false;
        }

        return !(mLine != null ? !mLine.equals(timeoLine.mLine) : timeoLine.mLine != null);

    }

    @Override
    public String toString() {
        return mLine.getName();
    }

}
