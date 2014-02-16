package fr.outadev.twistoast;

import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import fr.outadev.twistoast.timeo.TimeoRequestHandler;
import fr.outadev.twistoast.timeo.TimeoRequestObject;
import fr.outadev.twistoast.timeo.TimeoResultParser;
import fr.outadev.twistoast.timeo.TimeoScheduleObject;
import fr.outadev.twistoast.timeo.TimeoRequestHandler.EndPoints;

public class TwistoastPebbleReceiver extends PebbleDataReceiver {

	private static final UUID PEBBLE_UUID = UUID.fromString("020f9398-c407-454b-996c-6ac341337281");

	// message type key
	private static final int TWISTOAST_MESSAGE_TYPE = 0x00;

	// message type value
	private static final byte BUS_STOP_REQUEST = 0x10;
	private static final byte BUS_STOP_DATA_RESPONSE = 0x11;

	// message keys
	private static final int BUS_INDEX = 0x20;
	private static final int BUS_STOP_NAME = 0x21;
	private static final int BUS_DIRECTION_NAME = 0x22;
	private static final int BUS_LINE_NAME = 0x23;
	private static final int BUS_NEXT_SCHEDULE = 0x24;
	private static final int BUS_SECOND_SCHEDULE = 0x25;

	public TwistoastPebbleReceiver() {
		super(PEBBLE_UUID);
		Log.d("TwistoastPebbleReceiver", "initialized pebble listener");
	}

	@Override
	public void receiveData(final Context context, final int transactionId, PebbleDictionary data) {
		Log.d("TwistoastPebbleReceiver", "received a message from pebble " + PEBBLE_UUID);

		if((data.getInteger(TWISTOAST_MESSAGE_TYPE) == BUS_STOP_REQUEST)) {
			Log.d("TwistoastPebbleReceiver", "pebble request acknowledged");

			PebbleKit.sendAckToPebble(context, transactionId);
			databaseHandler = new TwistoastDatabase(context);

			short index = (data.getInteger(BUS_INDEX)).shortValue();
			final ArrayList<TimeoScheduleObject> stopsList = databaseHandler.getAllStops();
			final PebbleDictionary response = new PebbleDictionary();

			final short busIndex = (short) (index % stopsList.size());
			TimeoScheduleObject schedule = stopsList.get(busIndex);

			Log.d("TwistoastPebbleReceiver", "loading data for stop #" + busIndex + "...");

			// fetch schedule
			new AsyncTask<TimeoScheduleObject, Void, String>() {

				@Override
				protected String doInBackground(TimeoScheduleObject... params) {
					this.object = params[0];

					String url = TimeoRequestHandler
							.getFullUrlFromEndPoint(EndPoints.SCHEDULE, new TimeoRequestObject[] { new TimeoRequestObject(object
									.getLine().getId(), object.getDirection().getId(), object.getStop().getId()) });
					try {
						return TimeoRequestHandler.requestWebPage(url);
					} catch(Exception e) {
						e.printStackTrace();
					}

					return null;
				}

				@Override
				protected void onPostExecute(String result) {
					try {
						try {
							// parse the schedule and set it for our
							// TimeoScheduleObject, then refresh
							String[] scheduleArray = TimeoResultParser.parseSchedule(result);

							if(scheduleArray != null) {
								object.setSchedule(scheduleArray);
							} else {
								object.setSchedule(new String[] { context.getResources().getString(R.string.loading_error) });
							}

							Log.d("TwistoastPebbleReceiver", "got data for stop: " + object);

							response.addInt8(TWISTOAST_MESSAGE_TYPE, BUS_STOP_DATA_RESPONSE);
							response.addString(BUS_LINE_NAME, processStringForPebble(object.getLine().getName(), 10));
							response.addString(BUS_DIRECTION_NAME, processStringForPebble(object.getDirection().getName(), 15));
							response.addString(BUS_STOP_NAME, processStringForPebble(object.getStop().getName(), 15));
							response.addString(BUS_NEXT_SCHEDULE, processStringForPebble(object.getSchedule()[0], 15));
							response.addString(BUS_SECOND_SCHEDULE, (object.getSchedule().length > 1) ? processStringForPebble(object.getSchedule()[1], 15) : "");

							Log.d("TwistoastPebbleReceiver", "sending back: " + response);

							PebbleKit.sendDataToPebble(context, PEBBLE_UUID, response);

						} catch(ClassCastException e) {
							PebbleKit.sendNackToPebble(context, transactionId);
						}
					} catch(JSONException e) {
						object.setSchedule(new String[] { context.getResources().getString(R.string.loading_error) });
					} catch(ClassCastException e) {
						object.setSchedule(new String[] { context.getResources().getString(R.string.loading_error) });
					}
				}

				TimeoScheduleObject object;

			}.execute(schedule);

		} else {
			PebbleKit.sendNackToPebble(context, transactionId);
		}

	}
	
	private String processStringForPebble(String str, int length) {
		if(str == null) return "";
		
		// don't keep the part that's before the ":", it's making it less readable
		String[] stra = str.split(": ");
		str = (stra.length > 1) ? stra[1] : str;
		
		try {
			return str.substring(0, length);
		} catch(IndexOutOfBoundsException e) {
			return str;
		}
	}

	private TwistoastDatabase databaseHandler;

}
