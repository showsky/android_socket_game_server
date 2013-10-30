package com.miiitv.game.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.miiitv.game.server.Logger;

public class ServerService extends Service {
	
	private final static String TAG = "ServerService";
	private LocalBinder binder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		public ServerService getService() {
			return ServerService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Logger.i(TAG, "onBind");
		return binder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
}
