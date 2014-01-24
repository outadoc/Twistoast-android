package fr.outadev.twistoast.timeo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

public abstract class TimeoResultParser {

	public static String[] parseSchedule(String source) throws JSONException, ClassCastException {
		if(source != null) {
			String[] scheduleArray = new String[2];

			// parse the whole json array
			JSONArray resultArray = (JSONArray) new JSONTokener(source)
					.nextValue();

			if(resultArray != null && resultArray.getJSONObject(0) != null && resultArray
					.getJSONObject(0).getJSONArray("next") != null) {
				JSONArray scheduleJSONArray = resultArray.getJSONObject(0)
						.getJSONArray("next");

				for(int i = 0; i < scheduleJSONArray.length() && i < 2; i++) {
					scheduleArray[i] = scheduleJSONArray.getString(i);
				}

				return scheduleArray;
			}
		}

		return null;
	}

	public static ArrayList<String[]> parseMultipleSchedules(String source) throws JSONException, ClassCastException {
		if(source != null) {
			JSONArray resultArray = (JSONArray) new JSONTokener(source)
					.nextValue();

			ArrayList<String[]> dataList = new ArrayList<String[]>();

			for(int i = 0; i < resultArray.length(); i++) {
				if(resultArray != null && resultArray.getJSONObject(i) != null && resultArray
						.getJSONObject(i).getJSONArray("next") != null) {

					JSONArray scheduleJSONArray = resultArray.getJSONObject(i)
							.getJSONArray("next");

					dataList.add(i, new String[2]);

					for(int j = 0; j < scheduleJSONArray.length() && j < 2; j++) {
						dataList.get(i)[j] = scheduleJSONArray.getString(j);
					}
				}
			}

			return dataList;
		}

		return null;
	}

	public static ArrayList<TimeoIDNameObject> parseList(String source) throws JSONException, ClassCastException {
		if(source != null) {
			JSONArray resultArray = (JSONArray) new JSONTokener(source)
					.nextValue();

			if(resultArray != null) {
				ArrayList<TimeoIDNameObject> dataList = new ArrayList<TimeoIDNameObject>();

				for(int i = 0; i < resultArray.length(); i++) {
					String id = resultArray.optJSONObject(i).getString("id");
					String name = resultArray.optJSONObject(i)
							.getString("name");

					if(!id.equals("0")) {
						TimeoIDNameObject item = new TimeoIDNameObject(id, name);
						dataList.add(item);
					}
				}

				return dataList;
			}
		}

		return null;
	}

}
