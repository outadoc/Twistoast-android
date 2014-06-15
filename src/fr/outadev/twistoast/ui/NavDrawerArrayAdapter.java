package fr.outadev.twistoast.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.outadev.twistoast.MainActivity;
import fr.outadev.twistoast.R;

public class NavDrawerArrayAdapter extends ArrayAdapter<String> {

	public NavDrawerArrayAdapter(Context context, int resource, String[] objects, int selectedItemIndex) {
		super(context, resource, objects);
		this.selectedItemIndex = selectedItemIndex;
	}

	public NavDrawerArrayAdapter(Context context, int resource, String[] objects) {
		this(context, resource, objects, 0);
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView;

		if(position < getCount() - 1) {
			// if it's a normal row
			rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
		} else {
			// if it's a secondary row (ex: preferences)
			rowView = inflater.inflate(R.layout.drawer_list_item_pref, parent, false);
		}

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

	public int getSelectedItemIndex() {
		return selectedItemIndex;
	}

	public void setSelectedItemIndex(int selectedItemIndex) {
		this.selectedItemIndex = selectedItemIndex;
		notifyDataSetChanged();
	}

	private int selectedItemIndex;

}
