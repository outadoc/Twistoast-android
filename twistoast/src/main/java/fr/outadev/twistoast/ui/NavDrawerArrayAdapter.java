/*
 * Twistoast - NavDrawerArrayAdapter
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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.outadev.twistoast.IStopsListContainer;
import fr.outadev.twistoast.NavigationDrawerItem;
import fr.outadev.twistoast.NavigationDrawerSecondaryItem;
import fr.outadev.twistoast.R;

/**
 * An array adapter for the navigation drawer.
 *
 * @author outadoc
 */
public class NavDrawerArrayAdapter extends ArrayAdapter<NavigationDrawerItem> {

	private final IStopsListContainer container;
	private int selectedItemIndex;

	public NavDrawerArrayAdapter(Context context, IStopsListContainer container, int resource,
	                             List<NavigationDrawerItem> objects, int selectedItemIndex) {
		super(context, resource, objects);
		this.container = container;
		this.selectedItemIndex = selectedItemIndex;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		//convert the view if we haz to
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if(getItemViewType(position) == 1) {
				// if it's a secondary row (ex: preferences)
				convertView = inflater.inflate(R.layout.drawer_list_item_pref, parent, false);
			} else {
				// if it's a normal row
				convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
			}
		}

		TextView rowTitle = (TextView) convertView.findViewById(R.id.textTitle);
		rowTitle.setText(getContext().getResources().getString(getItem(position).getTitleResId()));

		if(position == selectedItemIndex) {
			rowTitle.setTypeface(null, Typeface.BOLD);
		} else {
			rowTitle.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		}

		rowTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedItemIndex = position;
				notifyDataSetChanged();
				container.loadFragmentFromDrawerPosition(position);
			}

		});

		return convertView;
	}

	public int getSelectedItemIndex() {
		return selectedItemIndex;
	}

	public void setSelectedItemIndex(int selectedItemIndex) {
		this.selectedItemIndex = selectedItemIndex;
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		// Define a way to determine which layout to use
		return (getItem(position) instanceof NavigationDrawerSecondaryItem) ? 1 : 0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

}
