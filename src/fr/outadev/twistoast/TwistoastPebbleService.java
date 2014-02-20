package fr.outadev.twistoast;

import com.getpebble.android.kit.PebbleKit;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class TwistoastPebbleService extends Service {

	private TwistoastPebbleReceiver receiver;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("TwistoastPebbleService", "initialized pebble listener");

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean usePebble = sharedPref.getBoolean("pref_pebble", false);
		
		if(usePebble) {
			receiver = new TwistoastPebbleReceiver();
			PebbleKit.registerReceivedDataHandler(this, receiver);
		} else {
			stopSelf();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

}
