/*
 * Twistoast - TimeoTest
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

package fr.outadev.android.timeo.test;

import android.os.AsyncTask;

import fr.outadev.android.timeo.KeolisRequestHandler;

public class TimeoTest {

	private KeolisRequestHandler handler;

	public TimeoTest() {
		handler = new KeolisRequestHandler();
		test1();
	}

	private void test1() {
		(new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... voids) {
				handler.getLines();
				return null;
			}

		}).execute();
	}

}
