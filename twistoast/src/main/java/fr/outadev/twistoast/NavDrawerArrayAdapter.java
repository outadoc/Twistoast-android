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

package fr.outadev.twistoast;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.outadev.twistoast.drawer.NavigationDrawerHeader;
import fr.outadev.twistoast.drawer.NavigationDrawerItem;
import fr.outadev.twistoast.drawer.NavigationDrawerSecondaryItem;
import fr.outadev.twistoast.drawer.NavigationDrawerSeparator;

/**
 * An array adapter for the navigation drawer.
 *
 * @author outadoc
 */
public class NavDrawerArrayAdapter extends ArrayAdapter<NavigationDrawerItem> {

	private static final int TYPE_NORMAL = 0;
	private static final int TYPE_SECONDARY = 1;
	private static final int TYPE_SEPARATOR = 2;
	private static final int TYPE_HEADER = 3;

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
		int itemType = getItemViewType(position);

		//convert the view if we haz to
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if(itemType == TYPE_SEPARATOR) {
				convertView = inflater.inflate(R.layout.drawer_list_separator, parent, false);
			} else if(itemType == TYPE_HEADER) {
				convertView = inflater.inflate(R.layout.drawer_list_header, parent, false);
			} else {
				convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
			}
		}

		//if we're dealing with a separator, we only need to inflate it, nothing more
		if(itemType == TYPE_SEPARATOR || itemType == TYPE_HEADER) {
			return convertView;
		}

		TextView rowTitle = (TextView) convertView.findViewById(R.id.lbl_drawer_item_title);
		ImageView rowIcon = (ImageView) convertView.findViewById(R.id.img_drawer_item_icon);

		if(itemType == TYPE_SECONDARY || getItem(position).getIconResId() == -1) {
			rowIcon.setVisibility(View.GONE);
		} else {
			rowIcon.setImageResource(getItem(position).getIconResId());
		}

		if(getItem(position).getTitleResId() != -1) {
			rowTitle.setText(getContext().getResources().getString(getItem(position).getTitleResId()));
		}

		rowTitle.setTypeface(null, Typeface.NORMAL);
		rowTitle.setSelected(false);
		rowTitle.setTextColor(Color.BLACK);

		rowIcon.setColorFilter(Color.rgb(100, 100, 100));

		if(position == selectedItemIndex) {
			rowTitle.setSelected(true);
			rowTitle.setTypeface(null, Typeface.BOLD);
			rowTitle.setTextColor(Colors.getColorPrimary(getContext()));

			rowIcon.setColorFilter(Colors.getColorPrimary(getContext()));
		}

		convertView.setOnClickListener(new OnClickListener() {

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
		if(getItem(position) instanceof NavigationDrawerSecondaryItem) {
			return TYPE_SECONDARY;
		} else if(getItem(position) instanceof NavigationDrawerSeparator) {
			return TYPE_SEPARATOR;
		} else if(getItem(position) instanceof NavigationDrawerHeader) {
			return TYPE_HEADER;
		}

		return TYPE_NORMAL;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

}
