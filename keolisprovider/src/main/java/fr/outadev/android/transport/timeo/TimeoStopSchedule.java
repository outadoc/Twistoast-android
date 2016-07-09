/*
 * Twistoast - TimeoStopSchedule
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

package fr.outadev.android.transport.timeo;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to store a list of schedules, with their corresponding line, direction, and stop
 * objects.
 *
 * @author outadoc
 */
public class TimeoStopSchedule {

    private TimeoStop mStop;
    private List<TimeoSingleSchedule> mSingleSchedules;
    private TimeoStopTrafficAlert mStopTrafficAlert;

    /**
     * Create a new schedule.
     *
     * @param stop      the stop this schedule corresponds to.
     * @param schedules a list of the schedules the stop is associated with.
     */
    public TimeoStopSchedule(TimeoStop stop, List<TimeoSingleSchedule> schedules) {
        this.mStop = stop;
        this.mSingleSchedules = schedules;
    }

    public TimeoStopSchedule() {
        this(null, new ArrayList<TimeoSingleSchedule>());
    }

    public TimeoStop getStop() {
        return mStop;
    }

    public void setStop(TimeoStop stop) {
        this.mStop = stop;
    }

    public List<TimeoSingleSchedule> getSchedules() {
        return mSingleSchedules;
    }

    public void setSchedules(List<TimeoSingleSchedule> schedules) {
        this.mSingleSchedules = schedules;
    }

    public TimeoStopTrafficAlert getStopTrafficAlert() {
        return mStopTrafficAlert;
    }

    public void setStopTrafficAlert(TimeoStopTrafficAlert stopTrafficAlert) {
        mStopTrafficAlert = stopTrafficAlert;
    }

    @Override
    public String toString() {
        return "NavitiaStopSchedule{" +
                "stop=" + mStop +
                ", schedules=" + mSingleSchedules +
                '}';
    }
}
