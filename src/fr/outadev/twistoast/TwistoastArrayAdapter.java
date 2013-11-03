package fr.outadev.twistoast;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.outadev.twistoast.timeo.TimeoRequestHandler;
import fr.outadev.twistoast.timeo.TimeoRequestObject;
import fr.outadev.twistoast.timeo.TimeoResultParser;
import fr.outadev.twistoast.timeo.TimeoScheduleObject;
import fr.outadev.twistoast.timeo.TimeoRequestHandler.EndPoints;

import android.content.Context;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TwistoastArrayAdapter extends ArrayAdapter<TimeoScheduleObject> {

	public TwistoastArrayAdapter(Context context, int resource,
			List<TimeoScheduleObject> objects) {
		super(context, resource, objects);

		this.objects = objects;
		this.context = context;

		updateScheduleData(true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// that's our row XML
		View rowView = inflater.inflate(R.layout.schedule_row, parent, false);

		// get all the stuff in it that we'll have to modify
		FrameLayout view_line_id = (FrameLayout) rowView
				.findViewById(R.id.view_line_id);

		TextView lbl_line = (TextView) rowView.findViewById(R.id.lbl_line_id);
		TextView lbl_stop = (TextView) rowView.findViewById(R.id.lbl_stop_name);
		TextView lbl_direction = (TextView) rowView
				.findViewById(R.id.lbl_direction_name);

		TextView lbl_schedule_1 = (TextView) rowView
				.findViewById(R.id.lbl_schedule_1);
		TextView lbl_schedule_2 = (TextView) rowView
				.findViewById(R.id.lbl_schedule_2);

		// line
		view_line_id.setBackgroundColor(TwistoastDatabase
				.getColorFromLineID(objects.get(position).getLine().getId()));
		lbl_line.setText(objects.get(position).getLine().getId());

		if(lbl_line.getText().length() > 3) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		} else if(lbl_line.getText().length() > 2) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
		} else {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		}

		// stop
		lbl_stop.setText(objects.get(position).getStop().getName());

		// direction
		lbl_direction.setText(objects.get(position).getDirection().getName());

		// schedule
		if(objects.get(position).getSchedule() != null 
				&& objects.get(position).getSchedule().length > 0
				&& objects.get(position).getSchedule()[0] != null) {
			lbl_schedule_1.setText("- "
					+ objects.get(position).getSchedule()[0]);
		} else {
			lbl_schedule_1.setText("- "
					+ context.getResources().getString(R.string.loading_data));
		}

		if(objects.get(position).getSchedule() != null 
				&& objects.get(position).getSchedule().length > 1
				&& objects.get(position).getSchedule()[1] != null) {
			lbl_schedule_2.setText("- "
					+ objects.get(position).getSchedule()[1]);
		}

		return rowView;
	}

	public void updateScheduleData(boolean resetBeforeFetch) {
		if(resetBeforeFetch) {
			for(int i = 0; i < objects.size(); i++) {
				objects.get(i).setSchedule(null);
			}

			notifyDataSetChanged();
		}

		for(int i = 0; i < objects.size(); i++) {
			GetTimeoDataFromAPITask task = new GetTimeoDataFromAPITask();
			task.execute(objects.get(i));
		}
	}

	private class GetTimeoDataFromAPITask extends
			AsyncTask<TimeoScheduleObject, Void, String> {
		@Override
		protected String doInBackground(TimeoScheduleObject... params) {
			this.object = params[0];

			String url = TimeoRequestHandler.getFullUrlFromEndPoint(
					EndPoints.SCHEDULE, new TimeoRequestObject(object.getLine()
							.getId(), object.getDirection().getId(), object
							.getStop().getId()));

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
						notifyDataSetChanged();
					} else {
						object.setSchedule(new String[] { context
								.getResources().getString(
										R.string.loading_error) });
					}
				} catch(ClassCastException e) {
					Toast.makeText(
							context,
							((JSONObject) new JSONTokener(result).nextValue())
									.getString("error"), Toast.LENGTH_LONG)
							.show();
				}
			} catch(JSONException e) {
				object.setSchedule(new String[] { context.getResources()
						.getString(R.string.loading_error) });
			} catch(ClassCastException e) {
				object.setSchedule(new String[] { context.getResources()
						.getString(R.string.loading_error) });
			}
		}

		public TimeoScheduleObject object;
	}

	private List<TimeoScheduleObject> objects;
	private Context context;

}
