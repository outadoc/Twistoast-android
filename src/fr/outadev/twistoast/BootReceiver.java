package fr.outadev.twistoast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// start background listener
		Intent pebbleIntent = new Intent(context, TwistoastPebbleService.class);
		context.startService(pebbleIntent);
	}

}
