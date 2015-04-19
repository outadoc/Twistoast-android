package fr.outadev.twistoast.background;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.List;
import java.util.UUID;

import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoStop;
import fr.outadev.android.timeo.TimeoStopSchedule;
import fr.outadev.twistoast.IWatchedStopChangeListener;
import fr.outadev.twistoast.Database;
import fr.outadev.twistoast.DatabaseOpenHelper;

public class PebbleTimeReceiver extends PebbleKit.PebbleDataReceiver {

	public static final UUID appUUID = UUID.fromString("49664da5-f3f9-47fd-87d0-574bb281e864");
	private static IWatchedStopChangeListener watchedStopStateListener = null;

	protected PebbleTimeReceiver() {
		super(appUUID);
	}

	@Override
	public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
		if(pebbleDictionary == null) {
			return;
		}

		switch(pebbleDictionary.getUnsignedIntegerAsLong(Keys.KEY_MESSAGE_TYPE).intValue()) {
			case MsgType.MSG_GET_STOPS:
				processGetStopsRequest(context, pebbleDictionary);
				break;
			case MsgType.MSG_WATCH:
				processWatchRequest(context, pebbleDictionary, true);
				break;
			case MsgType.MSG_UNWATCH:
				processWatchRequest(context, pebbleDictionary, false);
				break;
			case MsgType.MSG_GET_SCHEDULE:
				processGetScheduleRequest(context, pebbleDictionary);
				break;
		}
	}

	private void processGetStopsRequest(Context context, PebbleDictionary request) {
		Database db = new Database(DatabaseOpenHelper.getInstance(context));
		List<TimeoStop> stops = db.getAllStops();

		for(int i = 0; i < stops.size(); i++) {
			PebbleDictionary dict = craftStopInfoPacket(context, stops.get(i), i, stops.size());
			PebbleKit.sendDataToPebble(context, appUUID, dict);
		}
	}

	private void processWatchRequest(Context context, PebbleDictionary request, boolean watched) {
		Database db = new Database(DatabaseOpenHelper.getInstance(context));

		// Get the stop we want to get the schedule of
		TimeoStop stop = db.getStop(
				request.getString(Keys.KEY_STOP_ID),
				request.getString(Keys.KEY_LINE_ID),
				request.getString(Keys.KEY_DIR_ID),
				request.getInteger(Keys.KEY_NETWORK_CODE).intValue()
		);

		if(stop == null) {
			sendErrorToWatch(context, (byte) 0x00);
			return;
		}

		db.addToWatchedStops(stop);

		if(watchedStopStateListener != null) {
			watchedStopStateListener.onStopWatchingStateChanged(stop, watched);
		}
	}

	private void processGetScheduleRequest(final Context context, final PebbleDictionary request) {
		(new AsyncTask<Void, Void, PebbleDictionary>() {

			@Override
			protected PebbleDictionary doInBackground(Void... voids) {
				try {
					return craftSchedulePacket(context, request);
				} catch(Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(PebbleDictionary dict) {
				if(dict == null) {
					sendErrorToWatch(context, (byte) 0x00);
					return;
				}

				PebbleKit.sendDataToPebble(context, appUUID, dict);
			}

		}).execute();

	}

	private void sendErrorToWatch(Context context, byte errorCode) {
		PebbleDictionary dict = craftErrorPacket(context, errorCode);
		PebbleKit.sendDataToPebble(context, appUUID, dict);
	}

	private PebbleDictionary craftStopInfoPacket(Context context, TimeoStop stop, int index, int size) {
		PebbleDictionary response = new PebbleDictionary();

		response.addUint8(Keys.KEY_MESSAGE_TYPE, MsgType.MSG_STOP_INFO);
		response.addUint8(Keys.KEY_LIST_SIZE, (byte) size);
		response.addUint8(Keys.KEY_INDEX, (byte) index);
		response.addString(Keys.KEY_STOP_ID, stop.getId());
		response.addString(Keys.KEY_LINE_ID, stop.getLine().getId());
		response.addString(Keys.KEY_DIR_ID, stop.getLine().getDirection().getId());
		response.addUint16(Keys.KEY_NETWORK_CODE, (short) stop.getLine().getNetworkCode());
		response.addString(Keys.KEY_STOP_NAME, stop.getName());
		response.addString(Keys.KEY_LINE_NAME, stop.getLine().getName());
		response.addString(Keys.KEY_DIR_NAME, stop.getLine().getDirection().getName());
		response.addUint32(Keys.KEY_COLOR, Color.parseColor(stop.getLine().getColor()));
		response.addUint8(Keys.KEY_IS_WATCHED, (byte) (stop.isWatched() ? 1 : 0));

		return response;
	}

	private PebbleDictionary craftWatchStatusPacket(Context context, PebbleDictionary request) {
		PebbleDictionary response = new PebbleDictionary();
		Database db = new Database(DatabaseOpenHelper.getInstance(context));

		// Get the stop we want to get the schedule of
		TimeoStop stop = db.getStop(
				request.getString(Keys.KEY_STOP_ID),
				request.getString(Keys.KEY_LINE_ID),
				request.getString(Keys.KEY_DIR_ID),
				request.getInteger(Keys.KEY_NETWORK_CODE).intValue()
		);

		if(stop == null) {
			sendErrorToWatch(context, (byte) 0x00);
			return null;
		}

		response.addUint8(Keys.KEY_MESSAGE_TYPE, MsgType.MSG_WATCH_STATUS_CHANGED);
		response.addString(Keys.KEY_STOP_ID, stop.getId());
		response.addString(Keys.KEY_LINE_ID, stop.getLine().getId());
		response.addString(Keys.KEY_DIR_ID, stop.getLine().getDirection().getId());
		response.addUint16(Keys.KEY_NETWORK_CODE, (short) stop.getLine().getNetworkCode());
		response.addUint8(Keys.KEY_IS_WATCHED, (byte) (stop.isWatched() ? 1 : 0));

		return response;
	}

	private PebbleDictionary craftSchedulePacket(Context context, PebbleDictionary request) throws Exception {
		PebbleDictionary response = new PebbleDictionary();
		Database db = new Database(DatabaseOpenHelper.getInstance(context));

		// Get the stop we want to get the schedule of
		TimeoStop stop = db.getStop(
				request.getString(Keys.KEY_STOP_ID),
				request.getString(Keys.KEY_LINE_ID),
				request.getString(Keys.KEY_DIR_ID),
				request.getInteger(Keys.KEY_NETWORK_CODE).intValue()
		);

		if(stop == null) {
			sendErrorToWatch(context, (byte) 0x00);
			return null;
		}

		// Get the schedule (may throw an exception but we're not catching it here)
		TimeoStopSchedule schedule = TimeoRequestHandler.getSingleSchedule(stop);

		// Craft the packet
		response.addUint8(Keys.KEY_MESSAGE_TYPE, MsgType.MSG_SCHEDULE);
		response.addString(Keys.KEY_STOP_ID, stop.getId());
		response.addString(Keys.KEY_LINE_ID, stop.getLine().getId());
		response.addString(Keys.KEY_DIR_ID, stop.getLine().getDirection().getId());
		response.addInt16(Keys.KEY_NETWORK_CODE, (short) stop.getLine().getNetworkCode());

		// Only add schedules if there are any
		if(schedule.getSchedules() != null && schedule.getSchedules().size() > 0) {
			response.addString(Keys.KEY_NEXT_DEPARTURE_1, schedule.getSchedules().get(0).getShortFormattedTime(context));
		}

		if(schedule.getSchedules() != null && schedule.getSchedules().size() > 1) {
			response.addString(Keys.KEY_NEXT_DEPARTURE_1, schedule.getSchedules().get(1).getShortFormattedTime(context));
		}

		return response;
	}

	private PebbleDictionary craftErrorPacket(Context context, byte errorCode) {
		PebbleDictionary response = new PebbleDictionary();

		response.addUint8(Keys.KEY_MESSAGE_TYPE, MsgType.MSG_ERROR);
		response.addUint8(Keys.KEY_ERROR_CODE, errorCode);

		return response;
	}

	public static void setWatchedStopDismissalListener(IWatchedStopChangeListener watchedStopStateListener) {
		PebbleTimeReceiver.watchedStopStateListener = watchedStopStateListener;
	}

	private class MsgType {

		public static final byte MSG_ERROR = 0x00;

		public static final byte MSG_GET_STOPS = 0x01;
		public static final byte MSG_STOP_INFO = 0x02;
		public static final byte MSG_WATCH_STATUS_CHANGED = 0x04;
		public static final byte MSG_WATCH = 0x05;
		public static final byte MSG_UNWATCH = 0x06;
		public static final byte MSG_GET_SCHEDULE = 0x07;
		public static final byte MSG_SCHEDULE = 0x08;

	}

	private class Keys {

		public static final int KEY_MESSAGE_TYPE = 0x00;
		public static final int KEY_LIST_SIZE = 0x01;
		public static final int KEY_STOP_ID = 0x02;
		public static final int KEY_LINE_ID = 0x03;
		public static final int KEY_DIR_ID = 0x04;
		public static final int KEY_NETWORK_CODE = 0x05;
		public static final int KEY_STOP_NAME = 0x06;
		public static final int KEY_LINE_NAME = 0x07;
		public static final int KEY_DIR_NAME = 0x08;
		public static final int KEY_COLOR = 0x09;
		public static final int KEY_IS_WATCHED = 0x0A;
		public static final int KEY_ERROR_CODE = 0x0B;
		public static final int KEY_NEXT_DEPARTURE_1 = 0x0C;
		public static final int KEY_NEXT_DEPARTURE_2 = 0x0D;
		public static final int KEY_INDEX = 0x0E;

	}

}
