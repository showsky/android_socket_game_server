package com.miiitv.game.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.miiitv.game.server.EventType;
import com.miiitv.game.server.Logger;
import com.miiitv.game.server.config.Config;
import com.miiitv.game.server.gui.RankListener;

public class ServerService extends Service {
	
	private final static String TAG = "ServerService";
	private LocalBinder binder = new LocalBinder();
	private Server server = null;
	private HashMap<String, Client> pool = new HashMap<String, ServerService.Client>();
	public RankListener listener = null;
	private boolean isSelect = false;
	
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
	
	@Override
	public void onCreate() {
		super.onCreate();
		server = new Server();
		server.start();
	}
	
	public void broadcast(int type, JSONObject options, Client ownClient) {
		if(type == EventType.TYPE_START)
			isSelect = false;
		JSONObject json = new JSONObject();
		try {
			json.put("typs", EventType.TYPE_OPTIONS);
			if (options != null)
				json.putOpt("data", options);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (json.length() == 0)
			return;
		for (Entry<String, Client> client: pool.entrySet()) {
			if (ownClient != null && client.getValue() == ownClient)
				continue;
			Logger.d(TAG, "Send id: ", client.getKey(), " message: ", json.toString());
			client.getValue().ps.println(json.toString());
		}
	}
	
	public void broadcastWin(String facebookID) {
		if (TextUtils.isEmpty(facebookID))
			return;
		Client client = pool.get(facebookID);
		if (client != null) {
			try {
				JSONObject json = new JSONObject();
				json.put("type", EventType.TYPE_WIN);
				Logger.d(TAG, "Send win");
				client.ps.println(json.toString());
				broadcast(EventType.TYPE_END, null, client);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Logger.w(TAG, "broadcastWin error");
		}
	}
	
	class Client extends Thread {
		
		private String facebookID = null;
		private Socket socket = null;
		public PrintStream ps = null;
		public Scanner scanner = null;
		private boolean isRun = false;
		
		public Client(String facebookID, Socket socket, PrintStream ps, Scanner scanner) {
			this.scanner = scanner;
			this.ps = ps;
			this.socket = socket;
			isRun = true;
		}
		
		private void dispath(String receive) {
			if (TextUtils.isEmpty(receive))
				return;
			JSONObject json = null;;
			try {
				json = new JSONObject(receive);
				int type = json.getInt("type");
				switch (type) {
					case EventType.TYPE_SHOCK:
						if (isSelect)
							return;
						isSelect = true;
						JSONObject unlockJSON = new JSONObject();
						unlockJSON.put("type", EventType.TYPE_UNLOCK);
						ps.println(unlockJSON.toString());
						
						broadcast(EventType.TYPE_LOCK, null, this);
						break;
					case EventType.TYPE_ANSWER:
						int answer = json.getInt("data");
						if (listener != null)
							listener.matchAnswer(facebookID, answer);
						break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		private void stopClient() {
			if (scanner != null)
				scanner.close();
			if (ps != null)
				ps.close();
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			scanner = null;
			ps = null;
			socket = null;
			isRun = false;
		}
		
		@Override
		public void run() {
			super.run();
			while (isRun) {
				if (scanner.hasNext()) {
					String message = scanner.nextLine();
					Logger.d(TAG, "Receive facebookID: ", facebookID, " message: ", message);
					dispath(message);
				} else {
					stopClient();
					pool.remove(facebookID);
				}
			}
		}
	}
	
	class Server extends Thread {
		
		private boolean isRun = false;
		private ServerSocket ss = null;
		
		private void stopServer() {
			if (ss != null)
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			ss = null;
			isRun = false;
		}
		
		@Override
		public void run() {
			super.run();
			Logger.d(TAG, "Server run");
			Looper.prepare();
			try {
				ss  = new ServerSocket(Config.PORT);
				Socket socket = ss.accept();
				if (pool.size() < 5)
					socket.close();
				Logger.d(TAG, "Connect ip: ", socket.getInetAddress().toString());
				PrintStream ps = new PrintStream(socket.getOutputStream());
				Scanner scanner = new Scanner(new InputStreamReader(socket.getInputStream()));
				scanner.useDelimiter("\n");
				isRun = true;
				while (isRun) {
					if (scanner.hasNext()) {
						String message = scanner.nextLine();
						Logger.d(TAG, "Server Receive: ", message);
						try {
							JSONObject json = new JSONObject(message);
							String facebookID = json.getString("facebook_id");
							String facebookName = json.getString("facebook_name");
							int win = json.getInt("win");
							int lose = json.getInt("lose");
							if (listener != null)
								listener.join(facebookID, facebookName, win, lose);
							Client client = new Client(facebookID, socket, ps, scanner);
							client.start();
							pool.put(facebookID, client);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Looper.loop();
		}
	}
}
