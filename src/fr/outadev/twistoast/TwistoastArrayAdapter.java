package fr.outadev.twistoast;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.json.JSONException;

import fr.outadev.twistoast.timeo.TimeoRequestHandler;
import fr.outadev.twistoast.timeo.TimeoRequestObject;
import fr.outadev.twistoast.timeo.TimeoResultParser;
import fr.outadev.twistoast.timeo.TimeoScheduleObject;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TwistoastArrayAdapter extends ArrayAdapter<TimeoScheduleObject> {

	public TwistoastArrayAdapter(Context context, int resource, ArrayList<TimeoScheduleObject> objects) {
		super(context, resource, objects);

		this.objects = objects;
		this.context = context;
	}

	public ArrayList<TimeoScheduleObject> getObjects() {
		return objects;
	}

	public void setObjects(ArrayList<TimeoScheduleObject> objects) {
		this.objects = objects;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// that's our row XML
		View rowView = inflater.inflate(R.layout.schedule_row, parent, false);

		// get all the stuff in it that we'll have to modify
		FrameLayout view_line_id = (FrameLayout) rowView.findViewById(R.id.view_line_id);

		TextView lbl_line = (TextView) rowView.findViewById(R.id.lbl_line_id);
		TextView lbl_stop = (TextView) rowView.findViewById(R.id.lbl_stop_name);
		TextView lbl_direction = (TextView) rowView.findViewById(R.id.lbl_direction_name);

		TextView lbl_schedule_1 = (TextView) rowView.findViewById(R.id.lbl_schedule_1);
		TextView lbl_schedule_2 = (TextView) rowView.findViewById(R.id.lbl_schedule_2);

		// line
		view_line_id.setBackgroundColor(TwistoastDatabase.getColorFromLineID(objects.get(position).getLine().getId()));
		lbl_line.setText(objects.get(position).getLine().getId());

		if(lbl_line.getText().length() > 3) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		} else if(lbl_line.getText().length() > 2) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
		} else {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		}

		// stop
		lbl_stop.setText(context.getResources().getString(R.string.stop_name, objects.get(position).getStop().getName()));

		// direction
		lbl_direction.setText(context.getResources().getString(R.string.direction_name, objects.get(position).getDirection()
				.getName()));

		// schedule
		if(objects.get(position).getSchedule() != null && objects.get(position).getSchedule().length > 0 && objects.get(position)
				.getSchedule()[0] != null) {
			lbl_schedule_1.setText("- " + objects.get(position).getSchedule()[0]);
		} else {
			lbl_schedule_1.setText("- " + context.getResources().getString(R.string.loading_data));
		}

		if(objects.get(position).getSchedule() != null && objects.get(position).getSchedule().length > 1 && objects.get(position)
				.getSchedule()[1] != null) {
			lbl_schedule_2.setText("- " + objects.get(position).getSchedule()[1]);
		}

		view_line_id.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				MainActivity mainActivity = (MainActivity) TwistoastArrayAdapter.this.context;
				SparseBooleanArray checked = mainActivity.listView.getCheckedItemPositions();

				if(checked.get(position)) {
					mainActivity.listView.setItemChecked(position, false);
				} else {
					mainActivity.listView.setItemChecked(position, true);
				}
			}

		});

		return rowView;
	}

	public void updateScheduleData() {
		// start refreshing schedules
		GetTimeoDataFromAPITask task = new GetTimeoDataFromAPITask();
		task.execute();
	}

	private class GetTimeoDataFromAPITask extends AsyncTask<Void, Void, ArrayList<TimeoScheduleObject>> {

		@Override
		protected ArrayList<TimeoScheduleObject> doInBackground(Void... params) {
			TimeoRequestObject[] requestObj = new TimeoRequestObject[objects.size()];

			// add every stop to the request
			for(int i = 0; i < objects.size(); i++) {
				requestObj[i] = new TimeoRequestObject(objects.get(i).getLine().getId(), objects.get(i).getDirection().getId(), objects
						.get(i).getStop().getId());
			}

			final TimeoRequestHandler handler = new TimeoRequestHandler();
			ArrayList<TimeoScheduleObject> result = objects;

			try {
				try {
					result = handler.getMultipleSchedules(requestObj, objects);
				} catch(ClassCastException e) {
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							try {
								TimeoResultParser.displayErrorMessageFromTextResult(handler.getLastWebResponse(), ((Activity) context));
							} catch(JSONException e) {
								Toast.makeText(((Activity) context), handler.getLastWebResponse(), Toast.LENGTH_LONG).show();
								e.printStackTrace();
							}
						}
					});
				}
			} catch(JSONException e) {
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(((Activity) context), handler.getLastWebResponse(), Toast.LENGTH_LONG).show();
					}
				});
			} catch(final Exception e) {
				if(e instanceof IOException || e instanceof SocketTimeoutException) {
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(((Activity) context), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}

				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<TimeoScheduleObject> result) {
			if(result != null) {
				objects = result;
			}
			
			// refresh the display and callback MainActivity to end refresh
			notifyDataSetChanged();
			((MainActivity) context).endRefresh();
		}
	}

	private ArrayList<TimeoScheduleObject> objects;
	private Context context;

}
