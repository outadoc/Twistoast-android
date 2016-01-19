/*
 * Twistoast - IWatchedStopChangeListener
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

import fr.outadev.android.timeo.TimeoStop;

/**
 * A listener that will send a message when a watched bus has just arrived.
 * This is used, for example, in the main list, to hide the little "watched" icon when the bus has arrived
 * and the notifications are now effectively turned off.
 */
public interface IWatchedStopChangeListener {

    void onStopWatchingStateChanged(TimeoStop stop, boolean watched);

}
