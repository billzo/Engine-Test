package com.killercerealgames.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.TimeUtils;
import com.bda.controller.Controller;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.RubeSceneLoader;

public class MyEngineTest implements ApplicationListener{
	
	private MyActivityRequestHandler myRequestHandler;
	
	private static final float step = 1/60f;
	
	private RubeSceneLoader loader;
	private RubeScene scene;
	private double accumulator = 0;
	private World world;
	double newTime;
	double currentTime;
	double frameTime;
	
	private OrthographicCamera camera;
	private Box2DDebugRenderer renderer;
	private ShapeRenderer shapeRenderer;
	
	private Texture texture;
	private HashMap<Body, Shape> shapes;

	private SpriteBatch batch;
	
	private HashMap<String, Body> cameraBoundaries;
	private ArrayList<Body> boundaries;
	
	private Stage stage;
	
	private Body blue;
	private boolean debug = false;
	
	private RayHandler rayHandler;
	private PointLight pLight;
	
	private MyContactHandler myContactHandler;
	
	Array<Body> bodies;

	public static Controller mogaController1;
	public static boolean mogaEnabled = false;
	private boolean upReleased = true;
	private boolean downlReleased = true;
	private boolean rightReleased = true;
	private boolean leftReleased = true;
	
	public static int numberOfReds = 0;
	
	private long timeStarted;
	private long timeNow;
	public static long timeTaken;
	
	public MyEngineTest(MyActivityRequestHandler handler) {
		myRequestHandler = handler;
	}

	
	boolean isLocAvailable;
	public static HashMap<String, String> savedData;
	
	public class Shape {
		private String type;
		public Body body;
		public Sprite sprite;
		private PointLight pLight;
		public Shape (Body body, Sprite sprite, PointLight pLight, String type) {
			this.body = body;
			this.sprite = sprite;
			this.pLight = pLight;
			this.type = type;
		}
		public void delete() {
			if (pLight != null)
				pLight.remove();
		}
		public String getType() {
			return type;
		}
		
	}
	
	public class MyContactHandler implements ContactListener {
		
		public MyContactHandler() {}

		@Override
		public void beginContact(Contact contact) {
			
			Fixture A = contact.getFixtureA();
			Fixture B = contact.getFixtureB();
			
			if (A.getBody().getUserData() != null && B.getBody().getUserData() != null) {
				
				if (A.getBody().getUserData().equals("blue") && B.getBody().getUserData().equals("red")) {
					B.getBody().setUserData("DELETE");
				}
				else if (B.getBody().getUserData().equals("blue") && A.getBody().getUserData().equals("red")) {
					A.getBody().setUserData("DELETE");
				}
				
			}
			
		}

		@Override
		public void endContact(Contact contact) {}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {}
		
	}
	
	public class MySprite extends Sprite {
		public float myX;
		public float myY;
		
		public MySprite(Texture texture, float X1, float Y1) {
			super(texture);
			this.myX = X1;
			this.myY = Y1;
		}
	}
	
	public class ResetActor extends Actor {
		Texture newTex = new Texture(Gdx.files.internal("reset.png"));
		public MySprite resetSprite = new MySprite(newTex, 0, 0);
		
		public void init() {
			resetSprite.setSize(resetSprite.getWidth() * 0.25f, resetSprite.getHeight() * 0.25f);
			resetSprite.setOriginCenter();
			setColor(Color.CYAN);
			addListener(new InputListener() {
		        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
		            return true;  // must return true for touchUp event to occur
		        }
		        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
		        	respawn();
		        }
		    });
		}
		
		public void center() {
			resetSprite.setPosition(this.getX(), this.getY());
		}
		
		@Override
		public void draw (Batch batch, float parentAlpha) {
			resetSprite.draw(batch);
		}
	}

	public class MyActor extends Actor {
		public float changeX;
		
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Table table = new Table(skin);
		
		Texture texture = new Texture(Gdx.files.internal("newSpaceBackground.png"));
		MySprite sprite1 = new MySprite(texture, 0, 0);
		MySprite sprite2 = new MySprite(texture, sprite1.myX + sprite1.getWidth(), 0);
		MySprite sprite3 = new MySprite(texture, sprite1.myX + sprite1.getWidth(), 0);
		
		
		
        BitmapFont font = new BitmapFont();
        String stringTime;
        String bestTime;
		
		private MySprite currentSprite = sprite1;
		private MySprite nextSprite = sprite2;
		private MySprite nextNextSprite = sprite3;
		

		private Label numberOfRedsText = new Label("Red squares remaining: ", skin);
		private Label numberOfRedsNumber = new Label("", skin);
		private Label currentTimeText = new Label("Time elapsed: ", skin);
		private Label currentTimeNumber = new Label("", skin);
		private Label bestTimeText = new Label("Best time: ", skin);
		private Label bestTimeNumber = new Label("", skin);
		private Label previousTimeText = new Label("Previous time: ", skin);
		private Label previousTimeNumber = new Label("", skin);
		
		NumberFormat formatter = DecimalFormat.getInstance();
		
		public void init() {
			formatter.setMinimumFractionDigits(3);
			
			table.setPosition(Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 50);

			numberOfRedsText.setColor(Color.RED);
			numberOfRedsNumber.setColor(Color.RED);
			table.add(numberOfRedsText);
			table.add(numberOfRedsNumber);
			table.row();
			
			currentTimeText.setColor(Color.YELLOW);
			currentTimeNumber.setColor(Color.YELLOW);
			table.add(currentTimeText);
			table.add(currentTimeNumber);
			table.row();
			
			bestTimeText.setColor(Color.CYAN);
			bestTimeNumber.setColor(Color.CYAN);
			table.add(bestTimeText);
			table.add(bestTimeNumber);
			table.row();
			
			previousTimeText.setColor(Color.ORANGE);
			previousTimeNumber.setColor(Color.ORANGE);
			table.add(previousTimeText);
			table.add(previousTimeNumber);
			
			changeX = -currentSprite.getWidth();
			
			
		}

		@Override
		public void draw(Batch batch, float alpha) {
		    batch.draw(sprite1, sprite1.myX, sprite1.myY, sprite1.getOriginX(), sprite1.getOriginY(), sprite1.getWidth(), sprite1.getHeight(), sprite1.getScaleX(), sprite1.getScaleY(), sprite1.getRotation());
		    batch.draw(sprite2, sprite2.myX, sprite2.myY, sprite2.getOriginX(), sprite2.getOriginY(), sprite2.getWidth(), sprite2.getHeight(), sprite2.getScaleX(), sprite2.getScaleY(), sprite2.getRotation());
		    batch.draw(sprite3, sprite3.myX, sprite3.myY, sprite3.getOriginX(), sprite3.getOriginY(), sprite3.getWidth(), sprite3.getHeight(), sprite3.getScaleX(), sprite3.getScaleY(), sprite3.getRotation());

		    numberOfRedsNumber.setText(String.valueOf(MyEngineTest.numberOfReds));
		    currentTimeNumber.setText(formatter.format(MyEngineTest.timeTaken / 1000f));
		    bestTimeNumber.setText(formatter.format((Integer.parseInt(MyEngineTest.savedData.get("best_time")) / 1000f)));
		    previousTimeNumber.setText(formatter.format((Integer.parseInt(MyEngineTest.savedData.get("previous_time")) / 1000f)));
		    table.draw(batch, alpha);
		    
		}
		@Override
		public void act(float delta) {
			currentSprite.myX = currentSprite.myX - 1;
			nextSprite.myX = currentSprite.myX + currentSprite.getWidth();
			nextNextSprite.myX = nextSprite.myX + nextSprite.getWidth();
			if (currentSprite.myX <= changeX) {
				MySprite holder = currentSprite;
				currentSprite = nextSprite;
				nextSprite = nextNextSprite;
				nextNextSprite = holder;
				changeX = -currentSprite.getWidth();
			}

		}
	}
	
	public static class EncryptionHandler {
		
			private static String algorithm = "AES";
			private static byte[] keyValue = new byte[] {'4','3','9','1','2','3','8','8','4','5','3','5','1','1','0','4'};

			    public static char[] encrypt(String plainText) throws Exception 
			    {
			            Key key = generateKey();
			            Cipher chipher = Cipher.getInstance(algorithm);
			            chipher.init(Cipher.ENCRYPT_MODE, key);
			            byte[] encVal = chipher.doFinal(plainText.getBytes());
			            char[] encryptedValue = Base64Coder.encode(encVal);
			            return encryptedValue;
			    }

			    public static String decrypt(String encryptedText) throws Exception 
			    {
			            Key key = generateKey();
			            Cipher chiper = Cipher.getInstance(algorithm);
			            chiper.init(Cipher.DECRYPT_MODE, key);
			            byte[] decordedValue = Base64Coder.decode(encryptedText);
			            byte[] decValue = chiper.doFinal(decordedValue);
			            String decryptedValue = new String(decValue);
			            return decryptedValue;
			    }

			    private static Key generateKey() throws Exception 
			    {
			            Key key = new SecretKeySpec(keyValue, algorithm);
			            return key;
			    }
		}
	
	@Override
	public void create () {
		
		new EncryptionHandler();
		isLocAvailable = Gdx.files.isLocalStorageAvailable();
		if (isLocAvailable) {
			savedData = new HashMap<String, String>();
			loadData();
		}

		myContactHandler = new MyContactHandler();
	
		world = new World(new Vector2(0,-9.81f), true);
		world.setContactListener(myContactHandler);
		
		loader = new RubeSceneLoader(world);
		scene = loader.loadScene(Gdx.files.internal("ball.json"));
		
		camera = new OrthographicCamera(Gdx.graphics.getWidth() / 50, Gdx.graphics.getHeight() / 50);
		camera.zoom = 0.5f;
		renderer = new Box2DDebugRenderer();
		shapeRenderer = new ShapeRenderer();
		
		shapes = new HashMap<Body, Shape>();

		
		rayHandler = new RayHandler(world);
		rayHandler.setShadows(false);
		
		generateShapesAndCenterCamera();
		
		batch = new SpriteBatch();
		
		stage = new Stage();
		MyActor myActor = new MyActor();
		myActor.init();
		stage.addActor(myActor);
		ResetActor resetActor = new ResetActor();
		resetActor.init();
		resetActor.setSize(resetActor.resetSprite.getWidth(), resetActor.resetSprite.getHeight());
		resetActor.setPosition(0, Gdx.graphics.getHeight() - resetActor.resetSprite.getHeight());
		resetActor.center();
		stage.addActor(resetActor);
		
		bodies = new Array<Body>();
		
		Gdx.input.setInputProcessor(stage);


	}

	private void loadData() {
		if (!Gdx.files.local("data.sav").exists()) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(Gdx.files.local("data.sav").file()));
				String defaultSave = new String("best_time" + 60 * 1000 + ";");
				defaultSave += "previous_time" + 60 * 1000 + ";";
				writer.write(EncryptionHandler.encrypt(defaultSave));
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			loadData();
		}
		else {
			try {
				InputStream fis = new FileInputStream(Gdx.files.local("data.sav").file());
		        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
				
		        String readData = "";
		        String rawData = "";
		        
		        while ((readData = reader.readLine()) != null) {
		        	rawData += readData;
		        }

				String cleanData = EncryptionHandler.decrypt(new String(rawData));
				
				reader.close();

			    String info = "best_time";
			    String result = cleanData.substring(cleanData.indexOf(info) + info.length(), cleanData.indexOf(";", cleanData.indexOf(info)));
			    savedData.put(info, result);
			    info = "previous_time";
			    if (!cleanData.contains("previous_time")) {
				    savedData.put(info, "" + 60 * 1000);
			    }
			    else {
			    	result = cleanData.substring(cleanData.indexOf(info) + info.length(), cleanData.indexOf(";", 15));
				    savedData.put(info, result);
			    }


			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void saveData() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(Gdx.files.local("data_temp.sav").file()));
			String dataToSave = "";
			for(Entry<String, String> e : savedData.entrySet()) {
			        dataToSave += e.getKey();
			        dataToSave += e.getValue();
			        dataToSave += ";\n";
			}
			writer.write(EncryptionHandler.encrypt(dataToSave));
			writer.close();
			if (Gdx.files.local("data_temp.sav").exists()) {
				Gdx.files.local("data.sav").file().delete();
				Gdx.files.local("data_temp.sav").file().renameTo(Gdx.files.local("data.sav").file());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void generateShapesAndCenterCamera() {
		texture = new Texture(Gdx.files.internal("index.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		for (Body body : scene.getNamed(Body.class, "circle")) {
			Sprite sprite = new Sprite(texture);
			sprite.setScale(1/200f);
			sprite.setOriginCenter();
			
			pLight = new PointLight(rayHandler, 20, Color.RED, 0.90f, 0, 0);
			pLight.isXray();
			pLight.attachToBody(body);
			
			body.setUserData("red");
			
			shapes.put(body, new Shape(body, sprite, pLight, "red"));
			
			numberOfReds++;
		}

		
		

		
		texture = new Texture(Gdx.files.internal("blue.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		blue = scene.getNamed(Body.class, "square").first();
		blue.setUserData("blue");
		Sprite blueSprite = new Sprite(texture);
		blueSprite.setScale(1/750f);
		blueSprite.setOriginCenter();
		
		pLight = new PointLight(rayHandler, 20, Color.CYAN, 2, 0, 0);
		pLight.isXray();
		pLight.attachToBody(blue);
		
		Filter filter = new Filter();
		filter.categoryBits = 0;
		filter.maskBits = 0;
		
		PointLight.setContactFilter(filter);
		
		shapes.put(blue, new Shape(blue, blueSprite, pLight, "blue"));
		
		cameraBoundaries = new HashMap<String, Body>();
		boundaries = new ArrayList<Body>();
		createBoundaries();
		centerCamera();
		
		timeStarted = System.currentTimeMillis();
		timeNow = System.currentTimeMillis();
		timeTaken = timeNow - timeStarted;

		
	}


	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		stage.act(Gdx.graphics.getDeltaTime());

		
		newTime = TimeUtils.millis() / 1000.0;
		frameTime = Math.min(newTime - currentTime, 0.25f);
		
		currentTime = newTime;
		accumulator += frameTime;
		
		while (accumulator >= step) {
			world.step(step, 6, 2);
			accumulator -= step;
			interpolate((float) accumulator / step);
			moveCamera();
		}
		
		stage.draw();
		
		centerSprites();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (Shape shape : shapes.values()) {
			shape.sprite.draw(batch);
		}
		batch.end();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		if (debug) {
			stage.setDebugAll(true);
		}

		Vector2 vec1 = new Vector2();
		Vector2 vec2 = new Vector2();
		for (Body body : boundaries) {
			for (Fixture fixture : body.getFixtureList()) {
				ChainShape shape = (ChainShape) fixture.getShape();
				for (int i = 0; i < shape.getVertexCount(); i++) {
					shape.getVertex(i, vec2);
					shape.getVertex((i + 1) % shape.getVertexCount(), vec1);
					Gdx.gl20.glLineWidth(2.5f);
					shapeRenderer.setColor(33, 4, 0, 0.5f);
					shapeRenderer.line(vec1.x, vec1.y + 1.12f, vec2.x, vec2.y + 1.12f);
				}
			}



		}
		shapeRenderer.end();
		
		checkInput();
		if (debug) {
			renderer.render(world, camera.combined);
			System.out.println(world.getBodyCount());
		}
		

		
		rayHandler.setCombinedMatrix(camera.combined);
		rayHandler.updateAndRender();
		
		world.getBodies(bodies);
		for (Body body : bodies) {
			if (body.getUserData() != null && body.getUserData().equals("DELETE")) {
				Shape shape = shapes.get(body);
				if (shape.getType().equals("red")) {
					numberOfReds--;
				}
				shape.delete();
				shapes.remove(body);
				world.destroyBody(body);
			}
		}
		

		
		Gdx.graphics.setTitle("FPS: " + Gdx.graphics.getFramesPerSecond());
		timeNow = System.currentTimeMillis();
		timeTaken = timeNow - timeStarted;
		
		if (numberOfReds == 0) {
			savedData.put("previous_time", String.valueOf(timeTaken));
			if (timeTaken < Integer.parseInt((String)savedData.get("best_time"))) {
				savedData.put("best_time", String.valueOf(timeTaken));
				saveData();
			}
			respawn();
		}
		
	}
	
	private void respawn() {
		myRequestHandler.showAds(true);
		numberOfReds = 0;
		world.clearForces();
		world.getBodies(bodies);
		for (Body body : bodies) {
			Shape shape = shapes.get(body);
			if (shape == null) {}
			else {
				shape.delete();
				shapes.remove(body);
			}

			world.destroyBody(body);
		}

		loader = new RubeSceneLoader(world);
		scene = loader.loadScene(Gdx.files.internal("ball.json"));
		generateShapesAndCenterCamera();
		
	}
	
	private void moveCamera() {

//		float CAMERA_SPEED = .45f;
//		float delta = Gdx.graphics.getDeltaTime();
//		
//		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//			camera.position.x -= CAMERA_SPEED * delta;
//		}
//		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//			camera.position.x += CAMERA_SPEED * delta;
//
//		}
//		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
//			camera.position.y += CAMERA_SPEED * delta;
//
//		}
//		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//			camera.position.y -= CAMERA_SPEED * delta;
//		}
		
		Transform transform = blue.getTransform();
		Vector2 bodyPosition = transform.getPosition();
		Float rotation = MathUtils.radiansToDegrees * transform.getRotation();
		
//		float translateX = bodyPosition.x * alpha + camera.position.x * (1.0f - alpha);
//		float translateY = bodyPosition.y * alpha + camera.position.y * (1.0f - alpha);
		
		float speed = (float) Math.sqrt((blue.getLinearVelocity().x * blue.getLinearVelocity().x) + (blue.getLinearVelocity().y * blue.getLinearVelocity().y));
		speed = speed * .5f;
		float lerp = Gdx.graphics.getDeltaTime() * speed;
		Vector3 position = camera.position;
		position.x += (blue.getPosition().x - position.x) * lerp;
		position.y += (blue.getPosition().y - position.y) * lerp;
		
		camera.update();
	}

	private void centerSprites() {
		for (Shape shape : shapes.values()) {
			shape.sprite.setPosition(shape.body.getPosition().x - shape.sprite.getWidth() / 2, shape.body.getPosition().y - shape.sprite.getWidth() / 2);
		}
	}
	
	private void interpolate (float alpha) {
		for (Shape shape : shapes.values()) {
			Sprite sprite = shape.sprite;
			Transform transform = shape.body.getTransform();
			Vector2 bodyPosition = transform.getPosition();
			Float rotation = MathUtils.radiansToDegrees * transform.getRotation();
			
			sprite.setX(bodyPosition.x * alpha + sprite.getX() * (1.0f - alpha));
			sprite.setY(bodyPosition.y * alpha + sprite.getY() * (1.0f - alpha));
			sprite.setRotation(rotation * alpha + sprite.getRotation() * (1.0f - alpha));
			
		}

	}
	
	private void createBoundaries() {
//		cameraBoundaries.put("up", scene.getNamed(Body.class, "up").first());
//		cameraBoundaries.put("down", scene.getNamed(Body.class, "down").first());
//		cameraBoundaries.put("left", scene.getNamed(Body.class, "left").first());
//		cameraBoundaries.put("right", scene.getNamed(Body.class, "right").first());
		for (Body body : scene.getNamed(Body.class, "wall")) {
			boundaries.add(body);
		}
		
	}
	
	private void centerCamera() {
		//float centerX = (cameraBoundaries.get("left").getPosition().x + cameraBoundaries.get("right").getPosition().x) / 2;
		//float centerY = (cameraBoundaries.get("up").getPosition().y + cameraBoundaries.get("down").getPosition().y) / 2;
		//camera.position.set(centerX, centerY, 0);
		//camera.update();
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		saveData();
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {

		texture.dispose();
		rayHandler.dispose();
		world.dispose();
		renderer.dispose();
		
	}

	private void checkInput() {
		blue.setLinearDamping(0.75f);
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
			debug = !debug;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			blue.applyForceToCenter(new Vector2(0, 50), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			blue.applyForceToCenter(new Vector2(0, -50), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			blue.applyForceToCenter(new Vector2(-50, 0), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			blue.applyForceToCenter(new Vector2(50, 0), true);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			respawn();
		}
		
		if (Gdx.input.justTouched()) {
			Vector3 mousePos = new Vector3();
			mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(mousePos);
			
			Vector2 touch = new Vector2(mousePos.x, mousePos.y);
			Vector2 playerPos = new Vector2();
			playerPos.set(blue.getPosition());
			
			Vector2 destination = touch.sub(playerPos);
			destination.nor();
			destination.scl(50);
			if (destination.x > 50) destination.x = 50;
			if (destination.y > 50) destination.y = 50;

			blue.applyForceToCenter(destination, true);
			
		}
		
		if (mogaEnabled) {
			if (mogaController1.getKeyCode(Controller.KEYCODE_DPAD_UP) == Controller.ACTION_DOWN) {
				blue.applyForceToCenter(new Vector2(0, 50), true);
			}
			if (mogaController1.getKeyCode(Controller.KEYCODE_DPAD_DOWN) == Controller.ACTION_DOWN) {
				blue.applyForceToCenter(new Vector2(0, -50), true);
			}
			if (mogaController1.getKeyCode(Controller.KEYCODE_DPAD_RIGHT) == Controller.ACTION_DOWN) {
				blue.applyForceToCenter(new Vector2(50, 0), true);
			}
			if (mogaController1.getKeyCode(Controller.KEYCODE_DPAD_LEFT) == Controller.ACTION_DOWN) {
				blue.applyForceToCenter(new Vector2(-50, 0), true);
			}
			
		}
		
		int MAX_VELOCITY = 10;
		if (blue.getLinearVelocity().y <= -MAX_VELOCITY) {
			blue.setLinearVelocity(blue.getLinearVelocity().x, -MAX_VELOCITY);
		}
		if (blue.getLinearVelocity().y >= MAX_VELOCITY) {
			blue.setLinearVelocity(blue.getLinearVelocity().x, MAX_VELOCITY);
		}
		if (blue.getLinearVelocity().x >= MAX_VELOCITY) {
			blue.setLinearVelocity(MAX_VELOCITY, blue.getLinearVelocity().y);
		}
		if (blue.getLinearVelocity().x <= -MAX_VELOCITY) {
			blue.setLinearVelocity(-MAX_VELOCITY, blue.getLinearVelocity().y);
		}
		
	}

}








