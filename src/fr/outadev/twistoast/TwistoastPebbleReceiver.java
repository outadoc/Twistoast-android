package fr.outadev.twistoast;

import java.util.UUID;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoScheduleObject;
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
			TimeoScheduleObject schedule = databaseHandler.getStopAtIndex(busIndex);

			Log.d("TwistoastPebbleReceiver", "loading data for stop #" + busIndex + "...");

			// fetch schedule
			new AsyncTask<TimeoScheduleObject, Void, TimeoScheduleObject>() {

				@Override
				protected TimeoScheduleObject doInBackground(TimeoScheduleObject... params) {
					TimeoRequestHandler handler = new TimeoRequestHandler();
					TimeoScheduleObject schedule = params[0];

					try {
						schedule = handler.getSingleSchedule(schedule);
					} catch(Exception e) {
						PebbleKit.sendNackToPebble(context, transactionId);
						e.printStackTrace();
					}

					return schedule;
				}

				@Override
				protected void onPostExecute(TimeoScheduleObject schedule) {
					if(schedule != null) {
						// parse the schedule and set it for our
						// TimeoScheduleObject, then refresh
						String[] scheduleArray = schedule.getSchedule();

						if(scheduleArray == null) {
							schedule.setSchedule(new String[] { context.getResources().getString(R.string.loading_error) });
						}

						Log.d("TwistoastPebbleReceiver", "got data for stop: " + schedule);
						craftAndSendSchedulePacket(context, transactionId, schedule);
					} else {
						PebbleKit.sendNackToPebble(context, transactionId);
					}
				}

			}.execute(schedule);

		} else {
			PebbleKit.sendNackToPebble(context, transactionId);
		}

	}

	public void craftAndSendSchedulePacket(Context context, int transactionId, TimeoScheduleObject schedule) {
		PebbleDictionary response = new PebbleDictionary();

		response.addInt8(KEY_TWISTOAST_MESSAGE_TYPE, BUS_STOP_DATA_RESPONSE);
		response.addString(KEY_BUS_LINE_NAME, processStringForPebble(schedule.getLine().getName(), 10));
		response.addString(KEY_BUS_DIRECTION_NAME, processStringForPebble(schedule.getDirection().getName(), 15));
		response.addString(KEY_BUS_STOP_NAME, processStringForPebble(schedule.getStop().getName(), 15));
		response.addString(KEY_BUS_NEXT_SCHEDULE, processStringForPebble(schedule.getSchedule()[0], 15, true));
		response.addString(KEY_BUS_SECOND_SCHEDULE,
		        (schedule.getSchedule().length > 1) ? processStringForPebble(schedule.getSchedule()[1], 15, true) : "");

		if(schedule.getSchedule()[0].contains("imminent") || schedule.getSchedule()[0].contains("en cours")) {
			response.addInt8(KEY_SHOULD_VIBRATE, (byte) 1);
		}

		Log.d("TwistoastPebbleReceiver", "sending back: " + response);
		PebbleKit.sendDataToPebble(context, PEBBLE_UUID, response);
	}

	private String processStringForPebble(String str, int length) {
		return processStringForPebble(str, length, false);
	}

	private String processStringForPebble(String str, int length, boolean stripLine) {
		if(str == null) return "";

		if(stripLine) {
			// don't keep the part that's before the ":", it's making it less
			// readable
			String[] stra = str.split("Ligne ");
			str = (stra.length > 1) ? stra[1] : str;
		}

		try {
			return str.substring(0, length);
		} catch(IndexOutOfBoundsException e) {
			return str;
		}
	}

}
