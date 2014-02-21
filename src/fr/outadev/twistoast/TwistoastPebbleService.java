package fr.outadev.twistoast;

import com.getpebble.android.kit.PebbleKit;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class TwistoastPebbleService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		try {
			this.unregisterReceiver(receiver);
			Log.d("TwistoastPebbleService", "killed an existing pebble receiver");
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean usePebble = sharedPref.getBoolean("pref_pebble", false);

		if(usePebble) {
			Log.d("TwistoastPebbleService", "initialized pebble receiver");

			receiver = new TwistoastPebbleReceiver();
			PebbleKit.registerReceivedDataHandler(this, receiver);
		} else {
			stopSelf();
		}

		return START_STICKY;
	}

	@Override
    public IBinder onBind(Intent intent) {
	    // TODO Auto-generated method stub
	    return null;
    }
	
	private TwistoastPebbleReceiver receiver;

}
