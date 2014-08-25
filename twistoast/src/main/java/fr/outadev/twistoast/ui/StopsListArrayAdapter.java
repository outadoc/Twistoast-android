/*
 * Twistoast - StopsListArrayAdapter
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

package fr.outadev.twistoast.ui;

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

import java.util.ArrayList;
import java.util.Map;

import fr.outadev.android.timeo.model.TimeoStop;
import fr.outadev.android.timeo.model.TimeoStopSchedule;
import fr.outadev.twistoast.IStopsListContainer;
import fr.outadev.twistoast.R;
import fr.outadev.twistoast.database.TwistoastDatabase;

public class StopsListArrayAdapter extends ArrayAdapter<TimeoStop> {

	private final IStopsListContainer stopsListContainer;

	private ArrayList<TimeoStop> stops;
	private Map<TimeoStop, TimeoStopSchedule> schedules;

	public StopsListArrayAdapter(Context context, int resource, ArrayList<TimeoStop> stops,
	                             IStopsListContainer stopsListContainer) {
		super(context, resource, stops);

		this.stops = stops;
		this.stopsListContainer = stopsListContainer;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		TimeoStop currentItem = getItem(position);

		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.schedule_row, parent, false);
		}

		// get all the stuff in it that we'll have to modify
		FrameLayout view_line_id = (FrameLayout) convertView.findViewById(R.id.view_line_id);

		TextView lbl_line = (TextView) convertView.findViewById(R.id.lbl_line_id);
		TextView lbl_stop = (TextView) convertView.findViewById(R.id.lbl_stop_name);
		TextView lbl_direction = (TextView) convertView.findViewById(R.id.lbl_direction_name);

		TextView lbl_schedule_1 = (TextView) convertView.findViewById(R.id.lbl_schedule_1);
		TextView lbl_schedule_2 = (TextView) convertView.findViewById(R.id.lbl_schedule_2);

		LinearLayout view_traffic_message = (LinearLayout) convertView.findViewById(R.id.view_traffic_message);

		TextView lbl_message_title = (TextView) convertView.findViewById(R.id.lbl_message_title);
		TextView lbl_message_body = (TextView) convertView.findViewById(R.id.lbl_message_body);

		/*
		// set and make the message labels visible if necessary
		if(currentItem.getMessageTitle() != null && currentItem.getMessageBody() != null
				&& !currentItem.getMessageBody().isEmpty() && !currentItem.getMessageTitle().isEmpty()) {
			lbl_message_title.setText(currentItem.getMessageTitle());
			lbl_message_body.setText(currentItem.getMessageBody());

			view_traffic_message.setVisibility(View.VISIBLE);
		} else {
			view_traffic_message.setVisibility(View.GONE);
		}*/

		// line
		view_line_id.setBackgroundColor(TwistoastDatabase.getColorFromLineID(currentItem.getLine().getDetails()
				.getId()));
		lbl_line.setText(currentItem.getLine().getDetails().getId());

		if(lbl_line.getText().length() > 3) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		} else if(lbl_line.getText().length() > 2) {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
		} else {
			lbl_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		}

		// stop
		lbl_stop.setText(getContext().getResources().getString(R.string.stop_name, currentItem.getName()));

		// direction
		lbl_direction.setText(getContext().getResources()
				.getString(R.string.direction_name, currentItem.getLine().getDirection().getName()));

		if(schedules != null && schedules.get(currentItem) != null && schedules.get(currentItem).getSchedules() != null) {
			if(schedules.get(currentItem).getSchedules().size() > 0 && schedules.get(currentItem).getSchedules().get(0) !=
					null) {
				lbl_schedule_1.setText("- " + schedules.get(currentItem).getSchedules().get(0).getTime());
			} else {
				lbl_schedule_1.setText("- " + getContext().getResources().getString(R.string.loading_data));
			}

			if(schedules.get(currentItem).getSchedules().size() > 1 && schedules.get(currentItem).getSchedules().get(1) !=
					null) {
				lbl_schedule_2.setText("- " + schedules.get(currentItem).getSchedules().get(1).getTime());
			}
		} else {
			lbl_schedule_1.setText("- " + getContext().getResources().getString(R.string.loading_data));
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
				stopsListContainer.loadFragmentFromDrawerPosition(3);
			}

		});

		return convertView;
	}

	public void updateScheduleData() {
		// start refreshing schedules
		(new GetTimeoDataFromAPITask()).execute();
	}

	private class GetTimeoDataFromAPITask extends AsyncTask<Void, Void, Map<TimeoStop, TimeoStopSchedule>> {

		@Override
		protected Map<TimeoStop, TimeoStopSchedule> doInBackground(Void... params) {
			//TODO get and display data for new stops
			return null;
		}
	}

}
