package com.miiitv.game.server.gui;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.miiitv.game.server.Logger;

public class GameActivity extends Activity implements RankListener {
<<<<<<< HEAD
	private final static String	TAG			= "Game";
	private final static String	GAME		= "game";
	private final static String	OK			= "ok";
	private final static String	FAIL		= "fail";
	private Api					api;
	private Map<String, Player>	players;
	private String				_answer;
	private Context				mContext;
	private WebView				wv;
	private WebViewClient		mWebViewClient;
	private WebChromeClient		mWebChromeClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		init();

		join("jasonni1231", "BIG", "87", "69");

		wv = (WebView) findViewById(R.id.game);
		wv.setWebChromeClient(mWebChromeClient);
		wv.setWebViewClient(mWebViewClient);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new GameStart(), GAME);
		wv.loadUrl("file:///android_asset/example.html");
	}

	private void init() {
		api = Api.getInstance();
		mContext = this;
		players = new HashMap<String, Player>();

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
	}

	@Override
	public void join(String fbId, String fbName, String win, String lose) {
		if (TextUtils.isEmpty(fbId)) {
			return;
		}
		new Avatar().execute(fbId, fbName, win, lose);

		if (players.size() == 4) {
			new Play().execute();
		}
	}

	private class GameStart {

		@JavascriptInterface
		public void start() {
			Toast.makeText(mContext, "Game Start", Toast.LENGTH_LONG).show();
			selectAnswerer("Jason");
		}
	}

	/**
	 * answer question.
	 *
	 * @param fbId
	 */
	public void selectAnswerer(String fbId) {
		if (TextUtils.isEmpty(fbId)) {
			return;
		}
		wv.loadUrl("javascript:selectAnswerer('" + fbId + "');");
	}

	public void matchAnswer(String fbId, String questionId, String answer) {
		boolean isCorrect = TextUtils.equals(_answer, answer);
		String result = FAIL;
		JSONObject object;

		if (isCorrect) {
			result = OK;
			new SyncRank().execute(fbId, questionId, result);
		}
		try {
			object = new JSONObject();
			object.put("status", result);
			object.put("answer", questionId);
			wv.loadUrl("javascript:showResult('" + object.toString() + "');");

			Thread.sleep(3000);
			new Play().execute();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private class SyncRank extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			String fbId = params[0];
			String qId = params[1];
			players.get(fbId).rank = 1;

			result = api.syncRank(players.values(), qId);
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
//				wv.loadUrl("javascript:addPlayer('" + player.toString() + "');");
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
			}
		}
	}

	private class Play extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog	load		= null;
		private JSONObject		question	= null;

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
					_answer = question.getString("answer");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				wv.loadUrl("javascript:showQuestion('" + question.toString() + "');");
			}
		}
	}

=======
        private final static String     TAG                     = "Game";
        private final static String     GAME            = "game";
        private int                                     userCount       = 0;
        private String                          _answer;
        private WebView                         wv;
        private Context                         mContext;
        private Api                                     api;
        private Context                         mContext;
        private WebView                         wv;
        private WebViewClient           mWebViewClient;
        private WebChromeClient         mWebChromeClient;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.game);
                init();

                addPlayer("jasonni1231");

                wv = (WebView) findViewById(R.id.game);
                wv.setWebChromeClient(mWebChromeClient);
                wv.setWebViewClient(mWebViewClient);
                wv.getSettings().setJavaScriptEnabled(true);
                wv.addJavascriptInterface(new GameStart(), GAME);
                wv.loadUrl("file:///android_asset/layout.html");
        }

        private void init() {
                api = Api.getInstance();
                mContext = this;
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
        }

        public interface Callback {
        }

        public void addPlayer(String fbId) {
                if (TextUtils.isEmpty(fbId)) {
                        return;
                }
                new Avatar().execute(fbId);

                userCount += 1;
                if (userCount == 4) {
                        new Play().execute();
                }
        };

        private class GameStart {
                @JavascriptInterface
                public void start() {
                        Toast.makeText(mContext, "Game Start", Toast.LENGTH_LONG).show();
                        selectAnswerer("Jason");
                }
        }

        /**
         * answer question.
         *
         * @param fbId
         */
        public void selectAnswerer(String fbId) {
                if (TextUtils.isEmpty(fbId)) {
                        return;
                }
                wv.loadUrl("javascript:selectAnswerer('" + fbId + "');");
        }

        public void matchAnswer(String fbId, String answer) {
                boolean result = TextUtils.equals(_answer, answer);
                wv.loadUrl("javascript:showResult('" + String.valueOf(result) + "');");

                if (result) {
                        try {
                                Thread.sleep(3000);
                                new Play().execute();

                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
        }

        private class Avatar extends AsyncTask<String, Void, Boolean> {
                private JSONObject      player;

                @Override
                protected Boolean doInBackground(String... params) {
                        boolean result = false;
                        String fbId = params[0];
                        String avatarUrl = api.getAvatar(fbId);

                        if (!TextUtils.isEmpty(avatarUrl)) {
                                player = new JSONObject();
                                try {
                                        player.put("id", fbId);
                                        player.put("avatar", avatarUrl);
                                } catch (JSONException e) {
                                        e.printStackTrace();
                                }
                                result = true;
                        }
                        return result;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        if (result) {
                                wv.loadUrl("javascript:addPlayer('" + player.toString() + "');");
                        }
                }
        }

        private class Play extends AsyncTask<String, Void, Boolean> {

                private ProgressDialog  load            = null;
                private JSONObject              question        = null;

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
                        if (load != null) {
                                load.dismiss();
                                load = null;
                        }
                }

                @Override
                protected Boolean doInBackground(String... params) {
                        boolean result = false;
                        question = api.getQuestion();
                        if (question != null) {
                                result = true;
                        }
                        return result;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        if (load != null) {
                                load.dismiss();
                                load = null;
                        }
                        if (result) {
                                try {
                                        _answer = question.getString("answer");
                                } catch (JSONException e) {
                                        e.printStackTrace();
                                }
                                wv.loadUrl("javascript:showQuestion('" + question.toString() + "');");
                        }
                }
        }

        @Override
        public void join(String facebookID) {
                // TODO Auto-generated method stub
        }
>>>>>>> 2b24c572cc48e55beeb4fcfcfbcfc3143ff0bbbc
}
