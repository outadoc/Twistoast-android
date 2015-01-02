/*
 * Twistoast - ColorPickerDialog
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import fr.outadev.twistoast.R;

/**
 * Created by outadoc on 1/2/15.
 */
public class ColorPickerDialog extends DialogFragment {

	private Integer[] colors = {

	};

	private OnColorSelectedListener listener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.color_picker_dialog, null);
		GridView colorGrid = (GridView) rootView.findViewById(R.id.color_picker_grid);
		colorGrid.setAdapter(new ColorPickerAdapter(getActivity(), R.layout.color_picker_item, colors, this, listener));

		builder.setView(rootView);
		return builder.create();
	}

	public void setOnColorSelectedListener(OnColorSelectedListener listener) {
		this.listener = listener;
	}

}
