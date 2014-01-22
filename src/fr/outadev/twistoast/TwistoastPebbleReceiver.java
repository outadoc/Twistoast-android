package fr.outadev.twistoast;

import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;

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

	//message type key
	private static final int TWISTOAST_MESSAGE_TYPE = 0x00;
	
	//message type value
	private static final byte BUS_STOP_REQUEST = 0x10;
	private static final byte BUS_STOP_DATA_RESPONSE = 0x11;
	
	//message keys
	private static final int BUS_INDEX = 0x20;
	private static final int BUS_STOP_NAME = 0x21;
	private static final int BUS_DIRECTION_NAME = 0x22;
	private static final int BUS_LINE_ID = 0x23;
	private static final int BUS_LINE_NAME = 0x24;
	private static final int BUS_NEXT_SCHEDULE = 0x25;
	private static final int BUS_SECOND_SCHEDULE = 0x26;

	protected TwistoastPebbleReceiver(UUID arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public TwistoastPebbleReceiver() {
		super(PEBBLE_UUID);
	}

	@Override
	public void receiveData(final Context context, final int transactionId, PebbleDictionary data) {
		if((data.getInteger(TWISTOAST_MESSAGE_TYPE) == BUS_STOP_REQUEST)
				&& PebbleKit.areAppMessagesSupported(context)) {
			short busIndex = (data.getInteger(BUS_INDEX)).shortValue();
			
			databaseHandler = new TwistoastDatabase(context);
			ArrayList<TimeoScheduleObject> stopsList = databaseHandler.getAllStops();
			PebbleDictionary response = new PebbleDictionary();
			
			if(stopsList.size() < busIndex) {
				PebbleKit.sendNackToPebble(context, transactionId);
			} else {
				PebbleKit.sendAckToPebble(context, transactionId);
				
				TimeoScheduleObject schedule = stopsList.get(busIndex);
				
				//fetch schedule
				new AsyncTask<TimeoScheduleObject, Void, String>() {
					
		            @Override
		            protected String doInBackground(TimeoScheduleObject... params) {
		            	this.object = params[0];

		            	String url = TimeoRequestHandler.getFullUrlFromEndPoint(
		    					EndPoints.SCHEDULE, new TimeoRequestObject[]{
		    						new TimeoRequestObject(
		    							object.getLine().getId(), 
		    							object.getDirection().getId(), 
		    							object.getStop().getId()
		    						)});

		    			return TimeoRequestHandler.requestWebPage(url);
		            }

		            @Override
		    		protected void onPostExecute(String result) {
		    			try {
		    				try {
		    					// parse the schedule and set in for our
		    					// TimeoScheduleObject, then refresh
		    					String[] scheduleArray = TimeoResultParser
		    							.parseSchedule(result);

		    					if(scheduleArray != null) {
		    						object.setSchedule(scheduleArray);
		    					} else {
		    						object.setSchedule(new String[] { context
		    								.getResources().getString(
		    										R.string.loading_error) });
		    					}
		    				} catch(ClassCastException e) {
		    					PebbleKit.sendNackToPebble(context, transactionId);
		    				}
		    			} catch(JSONException e) {
		    				object.setSchedule(new String[] { context.getResources()
		    						.getString(R.string.loading_error) });
		    			} catch(ClassCastException e) {
		    				object.setSchedule(new String[] { context.getResources()
		    						.getString(R.string.loading_error) });
		    			}
		    		}
		            
		            TimeoScheduleObject object;
		            
		        }.execute(schedule);
				
				response.addInt8(TWISTOAST_MESSAGE_TYPE, BUS_STOP_DATA_RESPONSE);
				response.addInt16(BUS_INDEX, busIndex);
				response.addString(BUS_STOP_NAME, stopsList.get(busIndex).getStop().getName());
				response.addString(BUS_DIRECTION_NAME, stopsList.get(busIndex).getDirection().getName());
				response.addString(BUS_LINE_ID, stopsList.get(busIndex).getLine().getId());
				response.addString(BUS_LINE_NAME, stopsList.get(busIndex).getLine().getName());
				response.addString(BUS_NEXT_SCHEDULE, stopsList.get(busIndex).getSchedule()[0]);
				response.addString(BUS_SECOND_SCHEDULE, stopsList.get(busIndex).getSchedule()[1]);
				
				PebbleKit.sendDataToPebble(context, PEBBLE_UUID, response);
			}
		} else {
			PebbleKit.sendNackToPebble(context, transactionId);
		}
		
	}
	
	private TwistoastDatabase databaseHandler;

}
