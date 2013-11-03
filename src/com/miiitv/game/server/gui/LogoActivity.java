package com.miiitv.game.server.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.miiitv.game.R;
import com.miiitv.game.server.App;
import com.miiitv.game.server.Logger;

public class LogoActivity extends Activity {
	
	private final static String TAG = "LogoActivity";
	private final static int DELAY_TIME = 5 * 1000;
	private Context mContext = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.d(TAG, "onCreate");
		setContentView(R.layout.logo);
		mContext = this;
		App.getInstance().eventHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(mContext, GameActivity.class);
				startActivity(intent);
			}
		}, DELAY_TIME);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Logger.d(TAG, "onResume");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.d(TAG, "onDestory");
	}
}
