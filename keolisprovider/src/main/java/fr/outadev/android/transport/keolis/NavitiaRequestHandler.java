/*
 * Twistoast - NavitiaRequestHandler
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import fr.outadev.android.transport.IDirection;
import fr.outadev.android.transport.ILine;
import fr.outadev.android.transport.IRealTimeSchedule;
import fr.outadev.android.transport.IStopArea;
import fr.outadev.android.transport.ITrafficAlert;
import fr.outadev.android.transport.ITransportDataProvider;
import fr.outadev.android.transport.TransportAPIException;

/**
 * Handles all connections to the Twisto Realtime API.
 *
 * @author outadoc
 */
public class NavitiaRequestHandler implements ITransportDataProvider {

    @NonNull
    @Override
    public Iterable<ILine> getLines() throws TransportAPIException {
        return null;
    }

    @NonNull
    @Override
    public Iterable<IStopArea> getStops(ILine line, IDirection direction) throws TransportAPIException {
        return null;
    }

    @NonNull
    @Override
    public IRealTimeSchedule getRealTimeSchedule(IStopArea stop) throws TransportAPIException {
        return null;
    }

    @NonNull
    @Override
    public Iterable<IRealTimeSchedule> getRealTimeSchedule(Iterable<IStopArea> stops) throws TransportAPIException {
        return null;
    }

    @Nullable
    @Override
    public ITrafficAlert getGlobalTrafficAlert() {
        return null;
    }

}
