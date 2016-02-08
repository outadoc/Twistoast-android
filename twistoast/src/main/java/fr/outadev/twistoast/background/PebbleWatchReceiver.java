/*
 * Twistoast - PebbleWatchReceiver
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

package fr.outadev.twistoast.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.joda.time.DateTime;

import java.util.UUID;

import fr.outadev.android.transport.timeo.TimeoRequestHandler;
import fr.outadev.android.transport.timeo.TimeoSingleSchedule;
import fr.outadev.android.transport.timeo.TimeoStop;
import fr.outadev.android.transport.timeo.TimeoStopSchedule;
import fr.outadev.twistoast.Database;
import fr.outadev.twistoast.DatabaseOpenHelper;
import fr.outadev.twistoast.TimeFormatter;

/**
 * Receives and handles the Twistoast Pebble app requests in the background.
 *
 * @author outadoc
 */
public class PebbleWatchReceiver extends PebbleDataReceiver {

    private static final String TAG = "TwistoastPebbleReceiver";
    private static final UUID PEBBLE_UUID = UUID.fromString("020f9398-c407-454b-996c-6ac341337281");

    // message type key
    private static final int KEY_TWISTOAST_MESSAGE_TYPE = 0x00;

    // message type value
    private static final byte BUS_STOP_REQUEST = 0x10;
    private static final byte BUS_STOP_DATA_RESPONSE = 0x11;

    // message keys
    private static final int KEY_STOP_INDEX = 0x20;
    private static final int KEY_BUS_STOP_NAME = 0x21;
    private static final int KEY_BUS_DIRECTION_NAME = 0x22;
    private static final int KEY_BUS_LINE_NAME = 0x23;
    private static final int KEY_BUS_NEXT_SCHEDULE = 0x24;
    private static final int KEY_BUS_NEXT_SCHEDULE_DIR = 0x25;
    private static final int KEY_BUS_SECOND_SCHEDULE = 0x26;
    private static final int KEY_BUS_SECOND_SCHEDULE_DIR = 0x27;

    private static final int KEY_SHOULD_VIBRATE = 0x30;
    private static final int KEY_BACKGROUND_COLOR = 0x31;

    private SharedPreferences mPreferences;

    public PebbleWatchReceiver() {
        super(PEBBLE_UUID);
    }

    @Override
    public void receiveData(final Context context, final int transactionId, PebbleDictionary data) {
        Log.d(TAG, "received a message from pebble " + PEBBLE_UUID);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // open the database and count the stops
        Database databaseHandler = new Database(DatabaseOpenHelper.getInstance(context));
        int stopsCount = databaseHandler.getStopsCount();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // if we want a schedule and we have buses in the database
        if (data.getInteger(KEY_TWISTOAST_MESSAGE_TYPE) == BUS_STOP_REQUEST
                && stopsCount > 0 && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnected()) {
            Log.d(TAG, "pebble request acknowledged");
            PebbleKit.sendAckToPebble(context, transactionId);

            // get the bus index (modulo the number of stops there is in the db)
            final short busIndex = (short) (data.getInteger(KEY_STOP_INDEX).shortValue() % stopsCount);

            // get the stop that interests us
            final TimeoStop stop = databaseHandler.getStopAtIndex(busIndex);

            Log.d(TAG, "loading data for stop #" + busIndex + "...");

            // fetch schedule
            new AsyncTask<TimeoStop, Void, TimeoStopSchedule>() {

                @Override
                protected TimeoStopSchedule doInBackground(TimeoStop... params) {
                    TimeoStop stop = params[0];

                    try {
                        return TimeoRequestHandler.getSingleSchedule(stop);
                    } catch (Exception e) {
                        PebbleKit.sendNackToPebble(context, transactionId);
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(TimeoStopSchedule schedule) {
                    if (schedule != null) {
                        Log.d(TAG, "got data for stop: " + schedule);
                        craftAndSendSchedulePacket(context, schedule);
                    } else {
                        PebbleKit.sendNackToPebble(context, transactionId);
                    }
                }

            }.execute(stop);

        } else {
            PebbleKit.sendNackToPebble(context, transactionId);
        }

    }

    /**
     * Sens a response packet to the Pebble.
     *
     * @param context  a context
     * @param schedule the schedule to send back
     */
    private void craftAndSendSchedulePacket(Context context, TimeoStopSchedule schedule) {
        PebbleDictionary response = new PebbleDictionary();

        response.addInt8(KEY_TWISTOAST_MESSAGE_TYPE, BUS_STOP_DATA_RESPONSE);
        response.addString(KEY_BUS_LINE_NAME, processStringForPebble(schedule.getStop().getLine().getName(), 10));
        response.addString(KEY_BUS_DIRECTION_NAME,
                processStringForPebble(schedule.getStop().getLine().getDirection().getName(), 15));
        response.addString(KEY_BUS_STOP_NAME, processStringForPebble(schedule.getStop().getName(), 15));

        // Add first schedule time and direction to the buffer
        if (!schedule.getSchedules().isEmpty()) {
            // Time at which the next bus is planned
            DateTime nextSchedule = schedule.getSchedules().get(0).getScheduleTime();

            // Convert to milliseconds and add to the buffer
            response.addInt32(KEY_BUS_NEXT_SCHEDULE, (int) TimeFormatter.getDurationUntilBus(nextSchedule).getMillis());
            // Get a short version of the destination if required - e.g. A or B for the tram
            response.addString(KEY_BUS_NEXT_SCHEDULE_DIR, getOptionalShortDirection(schedule.getSchedules().get(0)));
        } else {
            response.addInt32(KEY_BUS_NEXT_SCHEDULE, -1);
            response.addString(KEY_BUS_NEXT_SCHEDULE_DIR, " ");
        }

        // Add the second schedule, same process
        if (schedule.getSchedules().size() > 1) {
            DateTime nextSchedule = schedule.getSchedules().get(1).getScheduleTime();

            response.addInt32(KEY_BUS_SECOND_SCHEDULE, (int) TimeFormatter.getDurationUntilBus(nextSchedule).getMillis());
            response.addString(KEY_BUS_SECOND_SCHEDULE_DIR, getOptionalShortDirection(schedule.getSchedules().get(1)));
        } else {
            response.addInt32(KEY_BUS_SECOND_SCHEDULE, -1);
            response.addString(KEY_BUS_SECOND_SCHEDULE_DIR, " ");
        }

        if (!schedule.getSchedules().isEmpty()) {
            DateTime scheduleTime = schedule.getSchedules().get(0).getScheduleTime();
            TimeFormatter.TimeDisplayMode displayMode = TimeFormatter.getTimeDisplayMode(scheduleTime, context);

            if (displayMode == TimeFormatter.TimeDisplayMode.ARRIVAL_IMMINENT
                    || displayMode == TimeFormatter.TimeDisplayMode.CURRENTLY_AT_STOP) {
                response.addInt8(KEY_SHOULD_VIBRATE, (byte) 1);
            }
        }

        int color = 0x0;

        if (mPreferences.getBoolean("pref_pebble_use_color", true)) {
            color = Color.parseColor(schedule.getStop().getLine().getColor());
        }

        response.addInt32(KEY_BACKGROUND_COLOR, color);

        Log.d(TAG, "sending back: " + response);
        PebbleKit.sendDataToPebble(context, PEBBLE_UUID, response);
    }

    private String getOptionalShortDirection(TimeoSingleSchedule schedule) {
        if (schedule.getDirection() != null && schedule.getDirection().matches("(A|B) .+")) {
            return schedule.getDirection().charAt(0) + "";
        } else {
            return " ";
        }
    }

    /**
     * Processes a string for the Pebble's screen.
     *
     * @param str       the string to process
     * @param maxLength the max length of the string
     * @return the processed string, or the original string if no action was required
     */
    private String processStringForPebble(String str, int maxLength) {
        if (str == null) {
            return "";
        }

        try {
            //truncate the string to [maxLength] characters, and add an ellipsis character at the end
            return str.substring(0, maxLength).trim() + "…";
        } catch (IndexOutOfBoundsException e) {
            //if the string is shorter than the max length, just return the string untouched
            return str;
        }
    }

}
