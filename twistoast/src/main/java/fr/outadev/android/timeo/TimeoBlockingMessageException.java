/*
 * Twistoast - TimeoBlockingMessageException
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

import android.app.AlertDialog;
import android.content.Context;

/**
 * A blocking message thrown by the API.
 * Should be displayed in an alert dialog.
 *
 * @author outadoc
 */
public class TimeoBlockingMessageException extends TimeoException {

	private String messageTitle;
	private String messageBody;

	public TimeoBlockingMessageException() {
		super();
	}

	public String getMessageTitle() {
		return messageTitle;
	}

	public void setMessageTitle(String messageTitle) {
		this.messageTitle = messageTitle;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	@Override
	public String getMessage() {
		return getMessageTitle();
	}

	public AlertDialog getAlertMessage(Context context) {
		return new AlertDialog.Builder(context)
				.setTitle(getMessageTitle())
				.setMessage(getMessageBody())
				.setNeutralButton("OK", null)
				.create();
	}
}
