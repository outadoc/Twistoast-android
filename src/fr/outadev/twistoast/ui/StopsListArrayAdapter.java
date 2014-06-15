package fr.outadev.twistoast.ui;

import java.util.ArrayList;

import org.json.JSONException;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import fr.outadev.android.timeo.TimeoRequestHandler;
import fr.outadev.android.timeo.TimeoResultParser;
import fr.outadev.android.timeo.TimeoScheduleObject;
import fr.outadev.twistoast.MainActivity;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.database.TwistoastDatabase;

public class StopsListArrayAdapter extends ArrayAdapter<TimeoScheduleObject> {

	public StopsListArrayAdapter(Context context, StopsListFragment fragment, int resource, ArrayList<TimeoScheduleObject> objects) {
		super(context, resource, objects);

		this.fragment = fragment;
		this.objects = objects;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TimeoScheduleObject currentItem = getItem(position);

		// that's our row XML
		View rowView = inflater.inflate(R.layout.schedule_row, parent, false);

		// get all the stuff in it that we'll have to modify
		FrameLayout view_line_id = (FrameLayout) rowView.findViewById(R.id.view_line_id);

		TextView lbl_line = (TextView) rowView.findViewById(R.id.lbl_line_id);
		TextView lbl_stop = (TextView) rowView.findViewById(R.id.lbl_stop_name);
		TextView lbl_direction = (TextView) rowView.findViewById(R.id.lbl_direction_name);

		TextView lbl_schedule_1 = (TextView) rowView.findViewById(R.id.lbl_schedule_1);
		TextView lbl_schedule_2 = (TextView) rowView.findViewById(R.id.lbl_schedule_2);

		LinearLayout view_traffic_message = (LinearLayout) rowView.findViewById(R.id.view_traffic_message);

		TextView lbl_message_title = (TextView) rowView.findViewById(R.id.lbl_message_title);
		TextView lbl_message_body = (TextView) rowView.findViewById(R.id.lbl_message_body);

		// set and make the message labels visible if necessary
		if(currentItem.getMessageTitle() != null && currentItem.getMessageBody() != null
		        && !currentItem.getMessageBody().isEmpty() && !currentItem.getMessageTitle().isEmpty()) {
			lbl_message_title.setText(currentItem.getMessageTitle());
			lbl_message_body.setText(currentItem.getMessageBody());
			
			view_traffic_message.setVisibility(View.VISIBLE);
		} else {
			view_traffic_message.setVisibility(View.GONE);
		}

		// line
		view_line_id.setBackgroundColor(TwistoastDatabase.getColorFromLineID(currentItem.getLine().getId()));
		lbl_line.setText(currentItem.getLine().getId());

		if(lbl_line.getText().length() > 3) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		} else if(lbl_line.getText().length() > 2) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
		} else {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		}

		// stop
		lbl_stop.setText(getContext().getResources().getString(R.string.stop_name, currentItem.getStop().getName()));

		// direction
		lbl_direction.setText(getContext().getResources()
		        .getString(R.string.direction_name, currentItem.getDirection().getName()));

		// schedule
		if(currentItem.getSchedule() != null && currentItem.getSchedule().length > 0 && currentItem.getSchedule()[0] != null) {
			lbl_schedule_1.setText("- " + currentItem.getSchedule()[0]);
		} else {
			lbl_schedule_1.setText("- " + getContext().getResources().getString(R.string.loading_data));
		}

		if(currentItem.getSchedule() != null && currentItem.getSchedule().length > 1 && currentItem.getSchedule()[1] != null) {
			lbl_schedule_2.setText("- " + currentItem.getSchedule()[1]);
		}

		view_line_id.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				SparseBooleanArray checked = ((ListView) parent).getCheckedItemPositions();

				if(checked.get(position)) {
					((ListView) parent).setItemChecked(position, false);
				} else {
					((ListView) parent).setItemChecked(position, true);
				}
			}

		});
		
		view_traffic_message.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) fragment.getActivity()).loadFragmentFromDrawerPosition(3);
			}

		});

		return rowView;
	}

	public void updateScheduleData() {
		// start refreshing schedules
		(new GetTimeoDataFromAPITask()).execute();
	}

	private class GetTimeoDataFromAPITask extends AsyncTask<Void, Void, ArrayList<TimeoScheduleObject>> {

		@Override
		protected ArrayList<TimeoScheduleObject> doInBackground(Void... params) {

			final TimeoRequestHandler handler = new TimeoRequestHandler();
			ArrayList<TimeoScheduleObject> result = objects;

			try {
				try {
					result = handler.getMultipleSchedules(objects);
				} catch(ClassCastException e) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								TimeoResultParser.displayErrorMessageFromTextResult(handler.getLastHTTPResponse(), getActivity());
							} catch(JSONException e) {
								if(!handler.getLastHTTPResponse().isEmpty()) {
									Toast.makeText(getActivity(), handler.getLastHTTPResponse(), Toast.LENGTH_LONG).show();
								}

								e.printStackTrace();
							}
						}
					});
				}
			} catch(final JSONException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(!handler.getLastHTTPResponse().isEmpty()) {
							Toast.makeText(getActivity(), handler.getLastHTTPResponse(), Toast.LENGTH_LONG).show();
						}

						e.printStackTrace();
					}
				});
			} catch(final HttpRequestException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.load_timeout),
						        Toast.LENGTH_LONG).show();
					}
				});

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
			fragment.endRefresh();
		}
	}

	private Activity getActivity() {
		if(getContext() instanceof Activity) {
			return (Activity) getContext();
		} else {
			return null;
		}
	}

	private final StopsListFragment fragment;
	private ArrayList<TimeoScheduleObject> objects;

}
