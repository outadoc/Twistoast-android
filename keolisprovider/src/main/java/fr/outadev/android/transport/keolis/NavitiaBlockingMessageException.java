/*
 * Twistoast - NavitiaBlockingMessageException
 * Copyright (C) 2013-2016 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.android.transport.keolis;

import android.app.AlertDialog;
import android.content.Context;

/**
 * A blocking message thrown by the API.
 * Should be displayed in an alert dialog.
 *
 * @author outadoc
 */
public class NavitiaBlockingMessageException extends NavitiaException {

    private String mMessageTitle;
    private String mMessageBody;

    public NavitiaBlockingMessageException() {
        super();
    }

    public String getMessageTitle() {
        return mMessageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.mMessageTitle = messageTitle;
    }

    public String getMessageBody() {
        return mMessageBody;
    }

    public void setMessageBody(String messageBody) {
        this.mMessageBody = messageBody;
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
