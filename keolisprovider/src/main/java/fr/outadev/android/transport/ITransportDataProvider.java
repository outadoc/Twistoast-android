/*
 * Twistoast - ITransportDataProvider
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

package fr.outadev.android.transport;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Baptiste on 2016-01-24.
 */
public interface ITransportDataProvider {

    /**
     * Fetch the bus lines from the API.
     */
    @NonNull
    Iterable<ILine> getLines() throws TransportAPIException;

    /**
     * Fetch a list of bus stops from the API.
     */
    @NonNull
    Iterable<IStopArea> getStops(ILine line, IDirection direction) throws TransportAPIException;

    /**
     * Fetches a schedule for a single bus stop from the API.
     */
    @NonNull
    IRealTimeSchedule getRealTimeSchedule(IStopArea stop) throws TransportAPIException;

    /**
     * Fetches schedules for multiple bus stops from the API.
     */
    @NonNull
    Iterable<IRealTimeSchedule> getRealTimeSchedule(Iterable<IStopArea> stops) throws TransportAPIException;

    /**
     * Fetches the current global traffic alert message. Might or might not be null.
     */
    @Nullable
    ITrafficAlert getGlobalTrafficAlert();
}