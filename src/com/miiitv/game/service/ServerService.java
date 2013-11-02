package com.miiitv.game.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
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
	private HashSet<Client> pool = new HashSet<ServerService.Client>();
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
		Logger.d(TAG, "onCreate");
		server = new Server();
		server.start();
	}
	
	public void broadcast(int type, JSONObject options, Client ownClient) {
		if(type == EventType.TYPE_START)
			isSelect = false;
		JSONObject json = new JSONObject();
		try {
			json.put("type", type);
			if (options != null)
				json.putOpt("data", options);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (json.length() == 0)
			return;
		for (Client client: pool) {
			if (ownClient != null && client == ownClient)
				continue;
			Logger.d(TAG, "Send message: ", json.toString());
			client.ps.println(json.toString());
		}
	}
	
	public void broadcastWin(String facebookID) {
		if (TextUtils.isEmpty(facebookID))
			return;
		Client winClient = null;
		for (Client client: pool) {
			if (client.facebookID.equals(facebookID)) {
				winClient = client;
				break;
			}
		}
		if (winClient != null) {
			try {
				JSONObject json = new JSONObject();
				json.put("type", EventType.TYPE_WIN);
				Logger.d(TAG, "Send win");
				winClient.ps.println(json.toString());
				broadcast(EventType.TYPE_END, null, winClient);
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
		
		public Client(Socket socket) {
			this.socket = socket;
			try {
				this.ps = new PrintStream(socket.getOutputStream());
				this.scanner = new Scanner(new InputStreamReader(socket.getInputStream()));
				this.scanner.useDelimiter("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
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
						Logger.i(TAG, "Type: EventType.TYPE_SHOCK");
						if (isSelect)
							return;
						isSelect = true;
						JSONObject unlockJSON = new JSONObject();
						unlockJSON.put("type", EventType.TYPE_UNLOCK);
						ps.println(unlockJSON.toString());
						broadcast(EventType.TYPE_LOCK, null, this);
						if (listener != null)
							listener.selectAnswerer(facebookID);
						break;
					case EventType.TYPE_ANSWER:
						Logger.i(TAG, "Type: EventType.TYPE_ANSWER");
						int answer = json.getInt("data");
						if (listener != null)
							listener.matchAnswer(facebookID, answer);
						break;
					case EventType.TYPE_JOIN:
						Logger.i(TAG, "Type: EventType.TYPE_JOIN");
						JSONObject dataJSON = json.getJSONObject("data");
						facebookID = dataJSON.getString("facebook_id");
						String facebookName = dataJSON.getString("facebook_name");
						int win = dataJSON.getInt("win");
						int lose = dataJSON.getInt("lose");
						if (listener != null)
							listener.join(facebookID, facebookName, win, lose);
						break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		private void stopClient() {
			Logger.e(TAG, "stop client");
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
			Looper.prepare();
			Logger.d(TAG , "Client start");
			isRun = true;
			while (isRun) {
				if (scanner.hasNext()) {
					String message = scanner.nextLine();
					Logger.d(TAG,  "Receive: ", message);
					dispath(message);
				} else {
					stopClient();
					pool.remove(this);
				}
			}
			Looper.loop();
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
				ss = new ServerSocket(Config.PORT);
				isRun = true;
				while (isRun) {
					Socket socket = ss.accept();
					if (pool.size() == 4)
						socket.close();
					Logger.d(TAG, "Connect ip: ", socket.getInetAddress().toString());
					Client client = new Client(socket);
					client.start();
					pool.add(client);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Looper.loop();
		}
	}
}
