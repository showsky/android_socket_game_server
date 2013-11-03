package com.miiitv.game.server.gui;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.miiicasa.game.bean.Player;
import com.miiitv.game.R;
import com.miiitv.game.server.Api;
import com.miiitv.game.server.App;
import com.miiitv.game.server.EventType;
import com.miiitv.game.server.Logger;

public class GameActivity extends Activity implements RankListener {

	private final static boolean	TEST_MODE		= false;
	private final static int		PEOPLE_NUM		= 2;
	private final static String		TAG				= "Game";
	private final static String		GAME			= "game";
	private final static String		OK				= "ok";
	private final static String		FAIL			= "fail";
	private final static String		TEST_ACCOUNT	= "jasonni1231";
	private final static int		START_SONG		= R.raw.eva_attachment;
	private final static int		WIN_SONG		= R.raw.ff_win;
	private final static int FAIL_SONG = R.raw.dead;
	private Api						api;
	private String					questionId;
	private Map<String, Player>		players;
	private int						_answer;
	private Context					mContext;
	private WebView					wv;
	private WebViewClient			mWebViewClient;
	private WebChromeClient			mWebChromeClient;
	// TODO
	private ClientCallbacks			dummyCallbacks;
	private MediaPlayThread	mediaPlayThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		init();

		if (TEST_MODE) {
			join(TEST_ACCOUNT, "BIG", 87, 69);
			join("showskytw", "BIG", 87, 69);
			join("tom", "BIG", 87, 69);
			join("jason", "BIG", 87, 69);
		}

		wv = (WebView) findViewById(R.id.game);
		wv.setWebChromeClient(mWebChromeClient);
		wv.setWebViewClient(mWebViewClient);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new GameStart(), GAME);
		// wv.loadUrl("file:///android_asset/example.html");
		wv.loadUrl("file:///android_asset/layout.html");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logger.i(TAG, "onResume");
		App.getInstance().serverService.listener = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Logger.i(TAG, "onPause");
		App.getInstance().serverService.listener = null;
	}

	private class MediaPlayThread extends Thread {
		
		private MediaPlayer	mediaPlayer;
		
		public MediaPlayThread(int type, boolean isLooping) {
			mediaPlayer = MediaPlayer.create(mContext, type);
			mediaPlayer.setLooping(isLooping);
		}

		@Override
		public void run() {
			mediaPlayer.start();
		}

		public void stopPlay() {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}

	private void init() {
		api = Api.getInstance();
		mContext = this;
		players = new HashMap<String, Player>();
		mediaPlayThread = new MediaPlayThread(START_SONG, true);
		Logger.d(TAG, "Start music player: START_SONG");
		mediaPlayThread.start();

		mWebViewClient = new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		};

		mWebChromeClient = new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				if ((title != null) && (title.trim().length() != 0)) {
					setTitle(title);
				}
			}
		};

		dummyCallbacks = new ClientCallbacks() {
			@Override
			public void gameStart() {
				try {
					selectAnswerer(TEST_ACCOUNT);
					Thread.sleep(3000);
					matchAnswer(TEST_ACCOUNT, 1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	}

	public interface ClientCallbacks {
		public void gameStart();
	}

	@Override
	public void join(String fbId, String fbName, int win, int lose) {
		Logger.d(TAG, "join()");
		new Avatar().execute(fbId, fbName, String.valueOf(win), String.valueOf(lose));
	}

	private class GameStart {

		@JavascriptInterface
		public void start() {
			Toast.makeText(mContext, "Game Start", Toast.LENGTH_SHORT).show();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/* NOTICE */
					if (TEST_MODE) {
						dummyCallbacks.gameStart();
					} else {
						App.getInstance().serverService.broadcast(EventType.TYPE_START, null, null);
					}
				}
			});
		}
	}

	@Override
	public void selectAnswerer(final String fbId) {
		Logger.d(TAG, "selectAnswerer()");
		if (TextUtils.isEmpty(fbId)) {
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				wv.loadUrl("javascript:selectAnswerer('" + fbId + "');");
			}
		});
	}

	@Override
	public void matchAnswer(String fbId, int answer) {
		Logger.d(TAG, "matchAnswer() fid: ", fbId, " answer: ", String.valueOf(answer));
		boolean isCorrect = (answer == _answer) ? true : false;
		String result = FAIL;
		JSONObject object;

		if (isCorrect) {
			Logger.d(TAG, "correct");
			result = OK;
			new SyncRank().execute(fbId, result);
			Logger.d(TAG, "start music play");
			mediaPlayThread = new MediaPlayThread(WIN_SONG, false);
			mediaPlayThread.start();
		} else {
			Logger.d(TAG, "faile");
			mediaPlayThread = new MediaPlayThread(FAIL_SONG, false);
			mediaPlayThread.start();
		}
		try {
			object = new JSONObject();
			object.put("status", result);
			object.put("answer", answer);
			wv.loadUrl("javascript:showResult('" + object.toString() + "');");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private class SyncRank extends AsyncTask<String, Void, Boolean> {

		private String	fbId	= null;

		@Override
		protected Boolean doInBackground(String... params) {
			fbId = params[0];
			boolean status = false;
			if (OK.equals(params[1])) {
				status = true;
				App.getInstance().serverService.broadcastWin(fbId);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {}
			}
			players.get(fbId).rank = 1;
			api.syncRank(players.values(), questionId);
			return status;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				/* NOTICE */
				mediaPlayThread.stopPlay();
				new Play().execute();
			}
		}
	}

	private class Avatar extends AsyncTask<String, Void, Boolean> {
		private Player	player;

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			String fbId = params[0];
			String avatarUrl = api.getAvatar(fbId);

			if (!TextUtils.isEmpty(avatarUrl)) {
				player = new Player();
				player.facebookID = fbId;
				player.facebookAvatar = avatarUrl;
				player.facebookName = params[1];
				player.win = Integer.parseInt(params[2]);
				player.lose = Integer.parseInt(params[3]);
				players.put(fbId, player);

				result = true;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {

				wv.loadUrl("javascript:addPlayer('" + player.toJSONString() + "');");
				Logger.d(TAG, String.valueOf(players.keySet().size()));
				if (players.size() == PEOPLE_NUM) {
					if (mediaPlayThread != null) {
						Logger.d(TAG, "music stop");
						mediaPlayThread.stopPlay();
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}
					new Play().execute();
				}
			}
		}
	}

	private class Play extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog	load		= null;
		private JSONObject		question	= null;
		
		public Play() {
			Logger.d(TAG, "Play asyncTask");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			load = new ProgressDialog(mContext);
			load.setCancelable(false);
			load.setCanceledOnTouchOutside(false);
			load.show();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Logger.d(TAG, "Cancel play");
			if (load != null && load.isShowing()) {
				load.dismiss();
				load = null;
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			question = api.getQuestion();
			if (question != null) {
				JSONObject options = null;
				try {
					options = question.getJSONObject("options");
					questionId = question.getString("question_id");

				} catch (JSONException e) {
					e.printStackTrace();
				}
				/* NOTICE */
				if (options != null && options.length() == 4)
					App.getInstance().serverService.broadcast(EventType.TYPE_OPTIONS, options, null);
				result = true;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (load != null && load.isShowing()) {
				load.dismiss();
				load = null;
			}
			if (result) {
				try {
					_answer = question.getInt("answer");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				wv.loadUrl("javascript:showQuestion('" + question.toString() + "');");
			}
		}
	}
}
