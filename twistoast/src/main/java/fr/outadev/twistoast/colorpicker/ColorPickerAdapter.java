/*
 * Twistoast - ColorPickerAdapter
 * Copyright (C) 2013-2015  Baptiste Candellier
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

package fr.outadev.twistoast.colorpicker;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import fr.outadev.twistoast.R;

/**
 * Created by outadoc on 1/2/15.
 */
public class ColorPickerAdapter extends ArrayAdapter<Integer> {

	private final DialogFragment fragment;
	private final OnColorSelectedListener listener;

	public ColorPickerAdapter(Context context, int resource, Integer[] objects, DialogFragment fragment,
	                          OnColorSelectedListener listener) {
		super(context, resource, objects);
		this.fragment = fragment;
		this.listener = listener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.color_picker_item, parent, false);
		}

		GradientDrawable colorItemBg = (GradientDrawable) convertView.getBackground();
		colorItemBg.setColor(getItem(position));

		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				fragment.dismiss();
				listener.onColorSelected(getItem(position));
			}

		});

		return convertView;
	}
}
