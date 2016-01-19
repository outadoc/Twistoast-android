/*
 * Twistoast - TimeoStop
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

import org.joda.time.DateTime;

/**
 * A bus stop.
 *
 * @author outadoc
 */
public class TimeoStop extends TimeoIDNameObject {

    private String mReference;
    private TimeoLine mLine;

    private boolean mIsOutdated;
    private boolean mIsWatched;

    private DateTime mLastETA;

    /**
     * Creates a stop.
     *
     * @param id   the id of the stop
     * @param name the name of the stop
     * @param ref  the reference of the stop
     */
    public TimeoStop(String id, String name, String ref, TimeoLine line) {
        super(id, name);

        this.mReference = ref;
        this.mLine = line;
        this.mIsWatched = false;
        this.mIsOutdated = false;
        this.mLastETA = null;
    }

    /**
     * Creates a stop, specifying if its notifications are active or not.
     *
     * @param id        the id of the stop
     * @param name      the name of the stop
     * @param ref       the reference of the stop
     * @param isWatched true if notifications are enabled for this stop, otherwise false
     */
    public TimeoStop(String id, String name, String ref, TimeoLine line, boolean isWatched) {
        this(id, name, ref, line);
        this.mIsWatched = isWatched;
    }

    /**
     * Creates a stop, specifying if its notifications are active or not.
     *
     * @param id        the id of the stop
     * @param name      the name of the stop
     * @param ref       the reference of the stop
     * @param isWatched true if notifications are enabled for this stop, otherwise false
     */
    public TimeoStop(String id, String name, String ref, TimeoLine line, boolean isWatched, DateTime lastETA) {
        this(id, name, ref, line, isWatched);
        this.mLastETA = lastETA;
    }

    public TimeoStop(TimeoLine line) {
        this.mLine = line;
    }

    public String getReference() {
        return mReference;
    }

    public void setReference(String ref) {
        this.mReference = ref;
    }

    public TimeoLine getLine() {
        return mLine;
    }

    public void setLine(TimeoLine line) {
        this.mLine = line;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Checks if this stop is outdated and its reference needs to be updated.
     *
     * @return true if it needs to be updated, otherwise false
     */
    public boolean isOutdated() {
        return mIsOutdated;
    }

    public void setOutdated(boolean isOutdated) {
        this.mIsOutdated = isOutdated;
    }

    /**
     * Checks if notifications are currently active for this bus stop.
     */
    public boolean isWatched() {
        return mIsWatched;
    }

    public void setWatched(boolean isWatched) {
        this.mIsWatched = isWatched;
    }

    /**
     * Gets the last estimated time of arrival for this bus stop.
     *
     * @return a timestamp of an approximation of the arrival of the next bus
     */
    public DateTime getLastETA() {
        return mLastETA;
    }

    public void setLastETA(DateTime lastETA) {
        this.mLastETA = lastETA;
    }
}
