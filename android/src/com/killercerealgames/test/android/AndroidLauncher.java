package com.killercerealgames.test.android;

import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.bda.controller.Controller;
import com.killercerealgames.test.MyEngineTest;

public class AndroidLauncher extends AndroidApplication {
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
		

		initialize(new MyEngineTest(), config);
	}
	@Override
	protected void onDestroy() {
		if (mogaController != null) {
			mogaController.exit();
		}
		super.onDestroy();
	}
}
