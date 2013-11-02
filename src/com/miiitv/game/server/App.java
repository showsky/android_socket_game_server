package com.miiitv.game.server;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.registry.RegistrationException;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;

import com.miiitv.game.server.config.Config;
import com.miiitv.game.service.ServerService;
import com.miiitv.game.upnp.BrowseRegistryListener;
import com.miiitv.game.utils.UpnpUtils;

public class App extends Application {
	
	private final static String TAG = "App";
	private static App instance = null;
	public AndroidUpnpService upnpService = null;
	public ServerService serverService = null;
	private HandlerThread handlerThread = null;
	public EventHandler eventHandler = null;
	private BrowseRegistryListener registryListener = new BrowseRegistryListener();
	private ServiceConnection upnpServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Logger.i(TAG, "Upnp onServiceDisconnected()");
			upnpService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Logger.i(TAG, "Upnp onServiceConnected()");
			upnpService = (AndroidUpnpService) service;
			upnpService.getRegistry().addListener(registryListener);
			String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); 
			Logger.w(TAG, "Android id: ", androidId);
			try {
				upnpService.getRegistry().addDevice(UpnpUtils.createDevice(androidId));
			} catch (RegistrationException e) {
				e.printStackTrace();
			} catch (ValidationException e) {
				e.printStackTrace();
			}
		}
	};
	private ServiceConnection serverServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Logger.i(TAG, "Server onServiceDisconnected()");
			serverService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Logger.i(TAG, "Server onServiceConnected()");
			serverService = ((ServerService.LocalBinder) service).getService();
		}
	};
	
	public App() {
		Logger.setProject(Config.PROJECT_NAME, Config.DEBUG_MODE);
	}
	
	public static App getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Logger.i(TAG, "onCreate");
		instance = this;
		initHandler();
		initServerService();
		initUpnp();
	}
	
	private void initHandler() {
		if (handlerThread == null) {
			handlerThread = new HandlerThread(TAG);
			handlerThread.start();
		}
		if (eventHandler == null)
			eventHandler = new EventHandler(handlerThread);
	}
	
	private void initUpnp() {
		Logger.w(TAG, "init Upnp");
		Intent intent = new Intent(getApplicationContext(), AndroidUpnpServiceImpl.class);
		bindService(intent, upnpServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void initServerService() {
		Logger.w(TAG, "init Server");
		Intent intent = new Intent(getApplicationContext(), ServerService.class);
		bindService(intent, serverServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void closeApp() {
		Logger.e(TAG, "Close App");
		if (handlerThread != null) {
			handlerThread.quit();
			handlerThread = null;
		}
		eventHandler = null;
		if (upnpService != null)
			unbindService(upnpServiceConnection);
		if (serverService != null)
			unbindService(serverServiceConnection);
	}
}
