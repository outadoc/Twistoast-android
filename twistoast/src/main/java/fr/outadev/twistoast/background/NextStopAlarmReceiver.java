/*
 * Twistoast - NextStopAlarmReceiver
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

package fr.outadev.twistoast.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import fr.outadev.twistoast.Utils;

public class NextStopAlarmReceiver extends BroadcastReceiver {

	private static final int ALARM_TYPE = AlarmManager.ELAPSED_REALTIME_WAKEUP;
	private static final int ALARM_FREQUENCY = 60 * 1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(Utils.TAG, "checking bookmarked stops");
	}

	public static void enable(Context context) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setInexactRepeating(ALARM_TYPE,
				SystemClock.elapsedRealtime() + 60 * 1000, ALARM_FREQUENCY, getBroadcast(context));
	}

	public static void disable(Context context) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(getBroadcast(context));
	}

	public static PendingIntent getBroadcast(Context context) {
		Intent intent = new Intent(context, NextStopAlarmReceiver.class);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

}
