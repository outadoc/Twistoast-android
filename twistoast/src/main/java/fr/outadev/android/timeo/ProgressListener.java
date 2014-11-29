/*
 * Twistoast - IProgressListener
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

package fr.outadev.android.timeo;

/**
 * Defines a progress listener.
 * Current process and maximum progress will be updated at will.
 */
public interface ProgressListener {

	/**
	 * Updates the current progress.
	 *
	 * @param current current progress, can't be greater than total
	 * @param total   maximum progress
	 */
	public void onProgress(int current, int total);

}
