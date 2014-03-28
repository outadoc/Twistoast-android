package fr.outadev.twistoast.ui;

import fr.outadev.twistoast.MainActivity;
import fr.outadev.twistoast.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NavDrawerArrayAdapter extends ArrayAdapter<String> {

	public NavDrawerArrayAdapter(Context context, int resource, String[] objects) {
		super(context, resource, objects);
		selectedItemIndex = 0;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// that's our row XML
		View rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);

		TextView rowTitle = (TextView) rowView.findViewById(R.id.textTitle);

		rowTitle.setText(getItem(position));

		if(position == selectedItemIndex) {
			rowTitle.setTypeface(null, Typeface.BOLD);
		}

		rowTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedItemIndex = position;
				notifyDataSetChanged();
				((MainActivity) getContext()).loadFragmentFromDrawerPosition(position);
			}

		});

		return rowView;
	}

	private int selectedItemIndex;

}
