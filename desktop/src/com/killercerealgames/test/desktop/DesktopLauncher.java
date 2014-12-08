package com.killercerealgames.test.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.killercerealgames.test.MyActivityRequestHandler;
import com.killercerealgames.test.MyEngineTest;

public class DesktopLauncher implements MyActivityRequestHandler {
	private static DesktopLauncher application;
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.samples = 2;
		if (application == null) {
			application = new DesktopLauncher();
		}
		new LwjglApplication(new MyEngineTest(application), config);
	}

	@Override
	public void showAds(boolean show) {
		// TODO Auto-generated method stub
		
	}
}
