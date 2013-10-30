package com.miiitv.game.server.gui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.miiitv.game.R;

public class GameActivity extends Activity {
	private WebView wv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		wv = (WebView) findViewById(R.id.game);
		wv.setWebChromeClient(mWebChromeClient);
		wv.setWebViewClient(mWebViewClient);
		wv.getSettings().setJavaScriptEnabled(true);
//		wv.loadUrl("http://www.google.com");
		wv.loadUrl("file:///android_asset/layout.html");
	}

	WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	};

	WebChromeClient mWebChromeClient = new WebChromeClient() {

		@Override
		public void onReceivedTitle(WebView view, String title) {
			if ((title != null) && (title.trim().length() != 0)) {
				setTitle(title);
			}
		}
	};
}
