package com.killercerealgames.test.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.bda.controller.Controller;
import com.killercerealgames.test.MyActivityRequestHandler;
import com.killercerealgames.test.MyEngineTest;

public class AndroidLauncher extends AndroidApplication implements MyActivityRequestHandler {
	
	private final static int TEST = 1;
	
	Controller mogaController = null;
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		mogaController = Controller.getInstance(this);
		try {
			if (mogaController.init());
			MyEngineTest.mogaController1 = mogaController;
			MyEngineTest.mogaEnabled = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		initialize(new MyEngineTest(this), config);
	}
	@Override
	protected void onDestroy() {
		if (mogaController != null) {
			mogaController.exit();
		}
		super.onDestroy();
	}
	
	protected static Handler handler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == TEST) {
				Gdx.app.log("MYTEST", "test");
			}
		}
	};
	@Override
	public void showAds(boolean show) {
		handler.sendEmptyMessage(show ? TEST : TEST);
		
	}
}
