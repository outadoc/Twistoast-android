/*
 * Twistoast - TwistoastPebbleReceiver
 * Copyright (C) 2013-2014  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

import fr.outadev.android.timeo.KeolisRequestHandler;
import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.android.timeo.model.TimeoStopSchedule;
import fr.outadev.twistoast.database.TwistoastDatabase;

public class TwistoastPebbleReceiver extends PebbleDataReceiver {

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
	private static final int KEY_BUS_SECOND_SCHEDULE = 0x25;

	private static final int KEY_SHOULD_VIBRATE = 0x30;

	public TwistoastPebbleReceiver() {
		super(PEBBLE_UUID);
		Log.d("TwistoastPebbleReceiver", "initialized pebble listener");
	}

	@Override
	public void receiveData(final Context context, final int transactionId, PebbleDictionary data) {
		Log.d("TwistoastPebbleReceiver", "received a message from pebble " + PEBBLE_UUID);

		// open the database and count the stops
		TwistoastDatabase databaseHandler = new TwistoastDatabase(context);
		int stopsCount = databaseHandler.getStopsCount();

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		// if we want a schedule and we have buses in the database
		if(data.getInteger(KEY_TWISTOAST_MESSAGE_TYPE) == BUS_STOP_REQUEST && stopsCount > 0 && cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnected()) {
			Log.d("TwistoastPebbleReceiver", "pebble request acknowledged");
			PebbleKit.sendAckToPebble(context, transactionId);

			// get the bus index (modulo the number of stops there is in the db)
			final short busIndex = (short) (data.getInteger(KEY_STOP_INDEX).shortValue() % stopsCount);

			// get the stop that interests us
			final TimeoStop stop = databaseHandler.getStopAtIndex(busIndex);

			Log.d("TwistoastPebbleReceiver", "loading data for stop #" + busIndex + "...");

			// fetch schedule
			new AsyncTask<TimeoStop, Void, TimeoStopSchedule>() {

				@Override
				protected TimeoStopSchedule doInBackground(TimeoStop... params) {
					KeolisRequestHandler handler = new KeolisRequestHandler();
					TimeoStop stop = params[0];

					try {
						return handler.getSingleSchedule(stop);
					} catch(Exception e) {
						PebbleKit.sendNackToPebble(context, transactionId);
						e.printStackTrace();
					}

					return null;
				}

				@Override
				protected void onPostExecute(TimeoStopSchedule schedule) {
					if(schedule != null) {
						// parse the schedule and set it for our
						// TimeoScheduleObject, then refresh

						if(schedule.getSchedules().size() == 0) {
							schedule.getSchedules().get(0).setTime(context.getResources().getString(R.string.loading_error));
						}

						Log.d("TwistoastPebbleReceiver", "got data for stop: " + schedule);
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

	public void craftAndSendSchedulePacket(Context context, TimeoStopSchedule schedule) {
		PebbleDictionary response = new PebbleDictionary();

		response.addInt8(KEY_TWISTOAST_MESSAGE_TYPE, BUS_STOP_DATA_RESPONSE);
		response.addString(KEY_BUS_LINE_NAME, processStringForPebble(schedule.getStop().getLine().getDetails().getName(), 10));
		response.addString(KEY_BUS_DIRECTION_NAME, processStringForPebble(schedule.getStop().getLine().getDirection().getName(),
				15));
		response.addString(KEY_BUS_STOP_NAME, processStringForPebble(schedule.getStop().getName(), 15));
		response.addString(KEY_BUS_NEXT_SCHEDULE, processStringForPebble(schedule.getSchedules().get(0).getShortFormattedTime
				(context), 15));
		response.addString(KEY_BUS_SECOND_SCHEDULE,
				(schedule.getSchedules().size() > 1) ? processStringForPebble(schedule.getSchedules().get(1)
						.getShortFormattedTime(context), 15) : "");

		//TODO update vibration pattern
		if(schedule.getSchedules().get(0).getTime().contains("imminent") || schedule.getSchedules().get(0).getShortFormattedTime
				(context).contains("en cours")) {
			response.addInt8(KEY_SHOULD_VIBRATE, (byte) 1);
		}

		Log.d("TwistoastPebbleReceiver", "sending back: " + response);
		PebbleKit.sendDataToPebble(context, PEBBLE_UUID, response);
	}

	private String processStringForPebble(String str, int length) {
		if(str == null) {
			return "";
		}

		try {
			return str.substring(0, length);
		} catch(IndexOutOfBoundsException e) {
			return str;
		}
	}

}
